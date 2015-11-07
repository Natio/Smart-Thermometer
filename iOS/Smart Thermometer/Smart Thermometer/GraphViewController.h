//
//  GraphViewController.h
//  Smart Thermometer
//
//  Created by Daniele Riccardelli on 26/10/2015.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "CorePlot-CocoaTouch.h"
#import <UIKit/UIKit.h>

@interface GraphViewController : UIViewController<CPTPlotDataSource>

@property (nonatomic, strong) NSArray *objects;

- (void)initPlot;

@end
