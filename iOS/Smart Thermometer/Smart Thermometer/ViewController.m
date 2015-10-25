//
//  ViewController.m
//  Smart Thermometer
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "ViewController.h"
#import <Parse/Parse.h>

@interface ViewController ()

@property (nonatomic, weak) IBOutlet UILabel *outside_temp;
@property (nonatomic, weak) IBOutlet UILabel *inside_temp;

@end

@implementation ViewController

- (IBAction) refreshData: (id)sender{
    [self refreshTemperatureData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self refreshTemperatureData];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)refreshTemperatureData{
    PFQuery *query = [PFQuery queryWithClassName:@"Temperatures"];
    [query orderByDescending:@"createdAt"];
    [query getFirstObjectInBackgroundWithBlock:^(PFObject * _Nullable object, NSError * _Nullable error) {
        if (object) {
            NSNumber *inside = object[@"inside"];
            NSNumber *outside = object[@"outside"];
            self.inside_temp.text = [NSString stringWithFormat:@"%@C", inside];
            self.outside_temp.text = [NSString stringWithFormat:@"%@C", outside];
        }
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
