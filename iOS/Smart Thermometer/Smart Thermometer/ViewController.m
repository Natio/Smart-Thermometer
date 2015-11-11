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
#define FAHRENHEIT_PREFERENCE 1
#define DAILY_PREFERENCE 0
#define WEEKLY_PREFERENCE 1
#define MONTHLY_PREFERENCE 2

@interface ViewController ()

@property (nonatomic, weak) IBOutlet UILabel *outside_temp;
@property (nonatomic, weak) IBOutlet UILabel *inside_temp;
@property (nonatomic, weak) IBOutlet UILabel *weather;
@property (nonatomic, weak) IBOutlet UIButton *refresh_button;

@end

@implementation ViewController

- (IBAction) refreshData: (id)sender{
    [self refreshTemperatureData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self refreshTemperatureData];
    
    [self fetchWeather];
    
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
            [NSException raise:@"Invalid foo value" format:@"Time frame %ld is invalid", (long)userMeasureUnit];
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

- (void)fetchWeather{
    NSString *URLString =[NSString stringWithFormat:@"http://api.openweathermap.org/data/2.5/weather?q=dublin,ie&appid=%s", WEATHER_API_KEY];
    NSURLSessionDataTask *URLSessionDataTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:URLString]
                                completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
                                    if (error) {
                                        return;
                                    }
            NSError *e = nil;
            NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData:data options: NSJSONReadingMutableContainers error: &e];
            self.weather.text = [JSON valueForKeyPath:@"weather.main"][0];
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
