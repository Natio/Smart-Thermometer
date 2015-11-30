//
//  ViewController.m
//  Smart Thermometer
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "GraphViewController.h"
#import "Keys.h"
#import "ViewController.h"
#import <Parse/Parse.h>

#define VIEW_SPACING 10.0
#define TOMORROW_SAMETIME_INDEX 7

@interface ViewController ()

@property (nonatomic, weak) IBOutlet UILabel *outside_temp;
@property (nonatomic, weak) IBOutlet UILabel *inside_temp;
@property (nonatomic, weak) IBOutlet UILabel *inside_forecast;
@property (nonatomic, weak) IBOutlet UILabel *weather;
@property (nonatomic, weak) IBOutlet UIButton *refresh_button;
@property (nonatomic, weak) IBOutlet UIPickerView *time_window_picker;

@end

@implementation ViewController

- (IBAction) refreshData: (id)sender{
    [self refreshTemperatureData];
    [self loadGraphData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self refreshTemperatureData];
    [self setupTimeWindowPicker];
    [self fetchAndSetWeather];
    [self loadGraphData];
    [self fetchAndSetForecast];
}

- (void)reloadTempertures{
    [self refreshTemperatureData];
}

- (void)loadGraphData{
    
    NSDate *now = [NSDate date];
    NSDate *startDate;
    
    NSInteger userMeasureUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"time_window"];
    switch (userMeasureUnit) {
        case DAILY_PREFERENCE:
            startDate = [now dateByAddingTimeInterval:-24*60*60];
            break;
        case WEEKLY_PREFERENCE:
            startDate = [now dateByAddingTimeInterval:-24*7*60*60];
            break;
        case MONTHLY_PREFERENCE:
            startDate = [now dateByAddingTimeInterval:-24*30*60*60];
            break;
        default:
            [NSException raise:@"Invalid time frame" format:@"Time frame %ld is invalid", (long)userMeasureUnit];
            break;
    }
    NSCalendarUnit unit = (NSCalendarUnit)(NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear);
    NSDateComponents *components = [[NSCalendar currentCalendar] components:unit fromDate:startDate];
    
    
    PFQuery *query = [PFQuery queryWithClassName:@"Hour"];
    [query whereKey:@"day" equalTo:@(components.day)];
    [query whereKey:@"month" equalTo:@(components.month-1)];
    [query whereKey:@"year" equalTo:@(components.year)];
    [query findObjectsInBackgroundWithBlock:^(NSArray * _Nullable objects, NSError * _Nullable error) {
        if (error) {
            NSLog(@"%@",error);
        }
        else{
            //create the controller for the view related to the
            //temeperature history graph that has to be put below.
            GraphViewController *graphViewController = [[GraphViewController alloc] init];
            graphViewController.objects = objects;
            [self addChildViewController:graphViewController];
            CGPoint origin = CGPointMake(0, CGRectGetMaxY(self.refresh_button.frame) + VIEW_SPACING);
            graphViewController.view.frame =CGRectMake(origin.x,
                                                       origin.y,
                                                       CGRectGetMaxX(self.view.bounds),
                                                       CGRectGetMaxY(self.view.bounds) - origin.y);
            //can now draw the plot as we know its positioning
            [graphViewController initPlot];
            [self.view addSubview:graphViewController.view];
            [graphViewController didMoveToParentViewController:self];
        }
    }];

}

- (void)fetchAndSetForecast{
    NSDateComponents *nowComponents = [[NSCalendar currentCalendar] components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear fromDate:[NSDate date]];
    PFQuery *query = [PFQuery queryWithClassName:@"Hour"];
    [query setLimit:20];
    [query orderByDescending:@"createdAt"];
    [query whereKey:@"hour" greaterThanOrEqualTo:[NSNumber numberWithFloat:MIN(0.0, nowComponents.hour - 1)]];
    [query whereKey:@"hour" lessThanOrEqualTo:[NSNumber numberWithInt:MAX(23.0, nowComponents.hour + 1)]];
    [query findObjectsInBackgroundWithBlock:^(NSArray * _Nullable objects, NSError * _Nullable error) {
        if (error) {
            NSLog(@"%@",error);
        }
        else{
            // calculate the expected tomorrow's house temperature, according to temperature history
            float aggregate_difference = 0.0;
            for (PFObject* parseObject in objects){
                float inside_temp = [parseObject[@"inside"] floatValue];
                float outside_temp = [parseObject[@"outside"] floatValue];
                NSLog(@"%f %f", inside_temp, outside_temp);
                aggregate_difference += inside_temp - outside_temp;
            }
            float avg_difference = aggregate_difference/objects.count;
            NSString *URLString =[NSString stringWithFormat:@"http://api.openweathermap.org/data/2.5/forecast?q=dublin,ie&appid=%@&units=metric", WEATHER_API_KEY];
            __block float forecast_temp;
            NSURLSessionDataTask *URLSessionDataTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:URLString]
                                                                                   completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
                    if (error) {
                        NSLog(@"%@", error);
                        return;
                    }
                    NSError *e = nil;
                    NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData:data options: NSJSONReadingMutableContainers error: &e];
                    NSDictionary *tomorrowSameTimeDictionary = [JSON valueForKeyPath:@"list"][TOMORROW_SAMETIME_INDEX];
                    forecast_temp = [(NSString *)([tomorrowSameTimeDictionary  valueForKeyPath:@"main.temp"]) floatValue];
                    NSNumberFormatter *fmt = [[NSNumberFormatter alloc] init];
                    [fmt setPositiveFormat:@"0.##"];
                    NSInteger userMeasureUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"measure_unit"];
                    dispatch_async(dispatch_get_main_queue(), ^{
                        self.inside_forecast.text = [NSString stringWithFormat:@"Tomorrow: %@%@", [fmt stringFromNumber:[NSNumber numberWithFloat:forecast_temp + avg_difference]], userMeasureUnit == FAHRENHEIT_PREFERENCE ? @"°F" : @"°C"];
                        });
            }];
            [URLSessionDataTask resume];
        }
    }];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)thePickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)thePickerView numberOfRowsInComponent:(NSInteger)component {
    return 3;
}

- (NSString *)pickerView:(UIPickerView *)thePickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    switch (row) {
        case DAILY_PREFERENCE:
            return @"Daily";
        case WEEKLY_PREFERENCE:
            return @"Weekly";
        case MONTHLY_PREFERENCE:
            return @"Monthly";
        default:
            [NSException raise:@"Invalid time frame" format:@"Time frame %ld is invalid", (long)row];
            break;
    }
    return nil;
}
- (void)pickerView:(UIPickerView *)thePickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    [[NSUserDefaults standardUserDefaults] setInteger:row forKey:@"time_window"];
    [self loadGraphData];
}

- (void)setupTimeWindowPicker{
    self.time_window_picker.dataSource = self;
    self.time_window_picker.delegate = self;
    [self.time_window_picker selectRow:[[NSUserDefaults standardUserDefaults] integerForKey:@"time_window"] inComponent:0 animated:NO];
}

- (void)fetchAndSetWeather{
    NSString *URLString =[NSString stringWithFormat:@"http://api.openweathermap.org/data/2.5/weather?q=dublin,ie&appid=%@", WEATHER_API_KEY];
    NSURLSessionDataTask *URLSessionDataTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:URLString]
                                completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
                                    if (error) {
                                        return;
                                    }
            NSError *e = nil;
            NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData:data options: NSJSONReadingMutableContainers error: &e];
            dispatch_async(dispatch_get_main_queue(), ^{
                self.weather.text = [JSON valueForKeyPath:@"weather.main"][0];
            });
    }];
    [URLSessionDataTask resume];
}

- (void)refreshTemperatureData{
    PFQuery *query = [PFQuery queryWithClassName:@"Temperatures"];
    [query orderByDescending:@"createdAt"];
    [query getFirstObjectInBackgroundWithBlock:^(PFObject * _Nullable object, NSError * _Nullable error) {
        if (object) {
            NSNumber *inside = object[@"inside"];
            NSNumber *outside = object[@"outside"];
            
            NSInteger userMeasureUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"measure_unit"];
            NSString *measureUnitString;
            
            if(userMeasureUnit == FAHRENHEIT_PREFERENCE){
                measureUnitString = @"°F";
                inside = [NSNumber numberWithFloat:[inside floatValue] * 9.0/5.0 + 32];
                outside = [NSNumber numberWithFloat:[outside floatValue] * 9.0/5.0 + 32];
            }else{
                measureUnitString = @"°C";
            }
            NSNumberFormatter *fmt = [[NSNumberFormatter alloc] init];
            [fmt setPositiveFormat:@"0.##"];
            self.inside_temp.text = [NSString stringWithFormat:@"%@%@", [fmt stringFromNumber:inside], measureUnitString];
            self.outside_temp.text = [NSString stringWithFormat:@"%@%@", [fmt stringFromNumber:outside], measureUnitString];
        }
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
