//
//  InterfaceController.m
//  Current Themperature Extension
//
//  Created by Paolo Coronati on 09/10/15.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "InterfaceController.h"
#import "STRowController.h"
#import <WatchConnectivity/WatchConnectivity.h>

@interface InterfaceController() <WCSessionDelegate>
@property (nonatomic, strong) WCSession * session;
@end


@implementation InterfaceController

- (void)sessionReachabilityDidChange:(WCSession *)session{
    if ([session isReachable]) {
        [self.session sendMessage:@{@"":@""} replyHandler:nil errorHandler:^(NSError * _Nonnull error) {
            NSLog(@"%@",error);
        }];
    }
}

- (void)session:(WCSession *)session didReceiveMessage:(NSDictionary<NSString *, id> *)message{
    if (message && [message[@"status"] isEqual:@"ok"]) {
        [self configureTableWithInsideTemperature:message[@"in"] outsideTemperature:message[@"out"]];
    }
}

- (void)awakeWithContext:(id)context {
    [super awakeWithContext:context];
    [self configureTableWithInsideTemperature:@"--" outsideTemperature:@"--"];
    self.session = [WCSession  defaultSession];
    self.session.delegate = self;
    [self.session activateSession];
    // Configure interface objects here.
}

- (void)configureTableWithInsideTemperature:(NSString *)inside outsideTemperature:(NSString *)outside{
    [self.table setNumberOfRows:2 withRowType:@"temp_row"];
    STRowController *r1 = [self.table rowControllerAtIndex:0];
    [r1.labelTitle setText:@"In"];
    [r1.labelTemperature setText:inside];
    STRowController *r2 = [self.table rowControllerAtIndex:1];
    [r2.labelTitle setText:@"Out"];
    [r2.labelTemperature setText:outside];
}

- (void)willActivate {
    
    [self.session sendMessage:@{@"":@""} replyHandler:nil errorHandler:^(NSError * _Nonnull error) {
        NSLog(@"%@",error);
    }];
    [super willActivate];
}

- (void)didDeactivate {
    // This method is called when watch view controller is no longer visible
    [super didDeactivate];
}

@end



