//
//  STRowController.h
//  Smart Thermometer
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <WatchKit/WatchKit.h>

@interface STRowController : NSObject
@property (nonatomic, strong) IBOutlet WKInterfaceLabel *labelTitle;
@property (nonatomic, strong) IBOutlet WKInterfaceLabel *labelTemperature;
@end
