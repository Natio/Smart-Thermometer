//
//  ViewController.m
//  Smart Thermometer
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "GraphViewController.h"
#import "ViewController.h"
#import <Parse/Parse.h>

#define VIEW_SPACING 10.0
#define FAHRENHEIT_PREFERENCE 1

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

    //create the controller for the view related to the
    //temeperature history graph that has to be put below.
    GraphViewController *graphViewController = [[GraphViewController alloc] init];
    CGPoint origin = CGPointMake(0, CGRectGetMaxY(self.refresh_button.frame) + VIEW_SPACING);
    graphViewController.view.frame =CGRectMake(origin.x,
                                               origin.y,
                                               CGRectGetMaxX(self.view.bounds),
                                               CGRectGetMaxY(self.view.bounds) - origin.y);
    //can now draw the plot as we know its positioning
    [graphViewController initPlot];
    
    [self.view addSubview:graphViewController.view];
    
}

- (void)fetchWeather{
    NSString *URLString =[NSString stringWithFormat:@"http://api.openweathermap.org/data/2.5/weather?q=dublin,ie&appid=%s", WEATHER_API_KEY];
    NSURLRequest *request = [NSURLRequest requestWithURL:
                             [NSURL URLWithString:URLString]];
    
    [[NSURLConnection alloc] initWithRequest:request delegate:self];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    NSMutableData *responseData = [[NSMutableData alloc] init];
    [responseData appendData:data];
    
    NSString *responseString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
    NSError *e = nil;
    NSData *jsonData = [responseString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData:jsonData options: NSJSONReadingMutableContainers error: &e];
    self.weather.text = [JSON valueForKeyPath:@"weather.main"][0];
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
