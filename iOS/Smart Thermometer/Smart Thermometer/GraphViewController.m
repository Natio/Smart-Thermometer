//
//  GraphViewController.m
//  Smart Thermometer
//
//  Created by Daniele Riccardelli on 26/10/2015.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "GraphViewController.h"
#import <Parse/Parse.h>

@interface GraphViewController ()

@property (nonatomic, weak) CPTGraphHostingView *hostView;
@property (nonatomic, strong) CPTPlot *outsidePlot, *insidePlot;

@end

@implementation GraphViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

#pragma mark - Chart behavior
-(void)initPlot {
    [self configureHost];
    [self configureGraph];
    [self configurePlots];
    [self configureAxes];
}

-(void)configureHost {
    CPTGraphHostingView *graphHostingView = [(CPTGraphHostingView *) [CPTGraphHostingView alloc] initWithFrame:self.view.bounds];
    self.hostView = graphHostingView;
    self.hostView.allowPinchScaling = YES;
    [self.view addSubview:self.hostView];
}

-(void)configureGraph {
    // 1 - Create the graph
    CPTGraph *graph = [[CPTXYGraph alloc] initWithFrame:self.hostView.bounds];
    [graph applyTheme:[CPTTheme themeNamed:kCPTDarkGradientTheme]];
    self.hostView.hostedGraph = graph;
    // 2 - Set graph title
    NSString *title = @"Temperatures: History";
    graph.title = title;
    // 3 - Create and set text style
    CPTMutableTextStyle *titleStyle = [CPTMutableTextStyle textStyle];
    titleStyle.color = [CPTColor whiteColor];
    titleStyle.fontName = @"Helvetica-Bold";
    titleStyle.fontSize = 16.0f;
    graph.titleTextStyle = titleStyle;
    graph.titlePlotAreaFrameAnchor = CPTRectAnchorTop;
    graph.titleDisplacement = CGPointMake(0.0f, 22.0f);
    // 4 - Set padding for plot area
    [graph.plotAreaFrame setPaddingLeft:30.0f];
    [graph.plotAreaFrame setPaddingBottom:30.0f];
    // 5 - Enable user interactions for plot space
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
    plotSpace.allowsUserInteraction = YES;
}

-(void)configurePlots {
    // 1 - Get graph and plot space
    CPTGraph *graph = self.hostView.hostedGraph;
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
    
    // 2 - Create the three plots
    CPTScatterPlot *outsidePlot = [[CPTScatterPlot alloc] init];
    self.outsidePlot = outsidePlot;
    outsidePlot.dataSource = self;
    outsidePlot.identifier = @"outside";
    CPTColor *outsideColor = [CPTColor blueColor];
    [graph addPlot:outsidePlot toPlotSpace:plotSpace];
    CPTScatterPlot *insidePlot = [[CPTScatterPlot alloc] init];
    self.insidePlot = insidePlot;
    insidePlot.dataSource = self;
    insidePlot.identifier = @"inside";
    CPTColor *insideColor = [CPTColor redColor];
    [graph addPlot:insidePlot toPlotSpace:plotSpace];
    
    // 3 - Set up plot space
    [plotSpace scaleToFitPlots:[NSArray arrayWithObjects:outsidePlot, insidePlot, nil]];
    CPTMutablePlotRange *xRange = [plotSpace.xRange mutableCopy];
    [xRange expandRangeByFactor:[NSNumber numberWithFloat:1.1f]];
    plotSpace.xRange = xRange;
    CPTMutablePlotRange *yRange = [plotSpace.yRange mutableCopy];
    [yRange expandRangeByFactor:[NSNumber numberWithFloat:2.0f]];
    plotSpace.yRange = yRange;
    
    // 4 - Create styles and symbols
    CPTMutableLineStyle *outsideLineStyle = [outsidePlot.dataLineStyle mutableCopy];
    outsideLineStyle.lineWidth = 2.5;
    outsideLineStyle.lineColor = outsideColor;
    outsidePlot.dataLineStyle = outsideLineStyle;
    CPTMutableLineStyle *outsideSymbolLineStyle = [CPTMutableLineStyle lineStyle];
    outsideSymbolLineStyle.lineColor = outsideColor;
    CPTPlotSymbol *outsideSymbol = [CPTPlotSymbol ellipsePlotSymbol];
    outsideSymbol.fill = [CPTFill fillWithColor:outsideColor];
    outsideSymbol.lineStyle = outsideSymbolLineStyle;
    outsideSymbol.size = CGSizeMake(6.0f, 6.0f);
    outsidePlot.plotSymbol = outsideSymbol;
    
    CPTMutableLineStyle *insideLineStyle = [insidePlot.dataLineStyle mutableCopy];
    insideLineStyle.lineWidth = 1.0;
    insideLineStyle.lineColor = insideColor;
    insidePlot.dataLineStyle = insideLineStyle;
    CPTMutableLineStyle *insideSymbolLineStyle = [CPTMutableLineStyle lineStyle];
    insideSymbolLineStyle.lineColor = insideColor;
    CPTPlotSymbol *insideSymbol = [CPTPlotSymbol starPlotSymbol];
    insideSymbol.fill = [CPTFill fillWithColor:insideColor];
    insideSymbol.lineStyle = insideSymbolLineStyle;
    insideSymbol.size = CGSizeMake(6.0f, 6.0f);
    insidePlot.plotSymbol = insideSymbol;
}

-(void)configureAxes {
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot{
    return [self.objects count];
}

-(NSNumber *)numberForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndex:(NSUInteger)index {
    //return [[self.objects objectAtIndex:index] objectForKey:@"outside"];
    switch (fieldEnum) {
        case CPTBarPlotFieldBarLocation:{
            return [[self.objects objectAtIndex:index] objectForKey:@"hour"];
        }
        default:
            break;
    }
    return [[self.objects objectAtIndex:index] objectForKey:(self.insidePlot == plot ? @"inside" : @"outside")];
}

-(nullable CPTLayer *)dataLabelForPlot:(nonnull CPTPlot *)plot recordIndex:(NSUInteger)idx{
    PFObject *obj = [self.objects objectAtIndex:idx];
    float value = 0.0;
    if (plot == self.insidePlot) {
        value = [[obj objectForKey:@"inside"] floatValue];
    }
    else{
        value = [[obj objectForKey:@"outside"] floatValue];
    }
    return [[CPTTextLayer alloc] initWithText:[NSString stringWithFormat:@"%.1f",value]];;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
