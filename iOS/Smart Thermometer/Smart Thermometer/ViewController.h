//
//  ViewController.h
//  Smart Thermometer
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import <UIKit/UIKit.h>

#define FAHRENHEIT_PREFERENCE 1
#define DAILY_PREFERENCE 0
#define WEEKLY_PREFERENCE 1
#define MONTHLY_PREFERENCE 2

@interface ViewController : UIViewController<UIPickerViewDataSource, UIPickerViewDelegate>


@end

