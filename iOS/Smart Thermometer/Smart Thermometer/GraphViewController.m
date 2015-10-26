//
//  GraphViewController.m
//  Smart Thermometer
//
//  Created by Daniele Riccardelli on 26/10/2015.
//  Copyright © 2015 Paolo Coronati. All rights reserved.
//

#import "GraphViewController.h"

@interface GraphViewController ()

@property (nonatomic, weak) CPTGraphHostingView *hostView;

@end

@implementation GraphViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
//    CPTXYGraph *graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
//    graph.title = @"Daily Graph";
//    graph.paddingLeft = 0;
//    graph.paddingTop = 0;
//    graph.paddingRight = 0;
//    graph.paddingBottom = 0;
//    // hide the axes
//    CPTXYAxisSet *axes = (CPTXYAxisSet *)[graph axisSet];
//    CPTMutableLineStyle *lineStyle = [[CPTMutableLineStyle alloc] init];
//    lineStyle.lineWidth = 0;
//    axes.xAxis.axisLineStyle = lineStyle;
//    axes.yAxis.axisLineStyle = lineStyle;
//    
//    // add a pie plot
//    CPTBarPlot *barPlot = [[CPTBarPlot alloc] init];
//    
//    CGFloat xMin = 0.0f;
//    CGFloat xMax = 6.0f;
//    CGFloat yMin = 0.0f;
//    CGFloat yMax = 800.0f;
//    
//    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *) graph.defaultPlotSpace;
//    
//    plotSpace.xRange = [CPTPlotRange plotRangeWithLocation:[NSNumber numberWithFloat:xMin] length:[NSNumber numberWithFloat:xMax]];
//    plotSpace.yRange = [CPTPlotRange plotRangeWithLocation:[NSNumber numberWithFloat:yMin] length:[NSNumber numberWithFloat:yMax]];
//    
//    barPlot.dataSource = self;
//    
//    CPTMutableLineStyle *barLineStyle = [[CPTMutableLineStyle alloc] init];
//    barLineStyle.lineColor = [CPTColor lightGrayColor];
//    barLineStyle.lineWidth = 0.5;
//    
//    barPlot.barWidth = [NSNumber numberWithFloat:10.0];
//    barPlot.barOffset = [NSNumber numberWithFloat:5.0];
//    barPlot.lineStyle = barLineStyle;
//
//    [graph addPlot:barPlot];
    
    // add a pie plot
//    CPTPieChart *pie = [[CPTPieChart alloc] init];
//    pie.dataSource = self;
//    pie.pieRadius = (self.view.frame.size.width * 0.9)/2;
//    [graph addPlot:pie];
//    
//    self.graphView.hostedGraph = graph;
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
    graph.titleDisplacement = CGPointMake(0.0f, 10.0f);
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
    outsidePlot.dataSource = self;
    outsidePlot.identifier = @"outside";
    CPTColor *outsideColor = [CPTColor blueColor];
    [graph addPlot:outsidePlot toPlotSpace:plotSpace];
    CPTScatterPlot *insidePlot = [[CPTScatterPlot alloc] init];
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
    [yRange expandRangeByFactor:[NSNumber numberWithFloat:1.2f]];
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
    return 0;
}

-(NSNumber *)numberForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndex:(NSUInteger)index {    return [NSNumber numberWithUnsignedInteger:0];
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