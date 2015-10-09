//
//  InterfaceController.h
//  Current Themperature Extension
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import <WatchKit/WatchKit.h>
#import <Foundation/Foundation.h>

@interface InterfaceController : WKInterfaceController

@property (nonatomic, strong) IBOutlet WKInterfaceTable *table;

@end
