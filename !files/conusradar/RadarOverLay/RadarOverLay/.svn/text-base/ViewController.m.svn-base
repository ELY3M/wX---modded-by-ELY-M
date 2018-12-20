//
//  ViewController.m
//  RadarOverLay
//
//  Created by Spark on 08/05/12.
//  Copyright (c) 2012 http://sugartin.info. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

@synthesize mapView = _mapView;
@synthesize mapOverlay = _mapOverlay;
@synthesize mapOverlayView = _mapOverlayView;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // create an overlay using the image
	self.mapOverlay = [[MapOverlay alloc] initWithLowerLeftCoordinate:CLLocationCoordinate2DMake(21.652538062803, -127.620375523875420) withUpperRightCoordinate:CLLocationCoordinate2DMake(50.406626367301044, -66.517937876818)];
    
    // add the custom overlay
    [self.mapView addOverlay:self.mapOverlay];
    
    // set the co-ordinates & zoom to specificly USA.
    MKMapPoint lowerLeft = MKMapPointForCoordinate(CLLocationCoordinate2DMake(21.652538062803, -127.620375523875420));
    MKMapPoint upperRight = MKMapPointForCoordinate(CLLocationCoordinate2DMake(50.406626367301044, -66.517937876818));
    
    MKMapRect mapRect = MKMapRectMake(lowerLeft.x, upperRight.y, upperRight.x - lowerLeft.x, lowerLeft.y - upperRight.y);
    
    [self.mapView setVisibleMapRect:mapRect animated:YES];
}

-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id)overlay{
    if([overlay isKindOfClass:[MapOverlay class]]) {
        MapOverlay *mapOverlay = overlay;
        if(!self.mapOverlayView) {
            self.mapOverlayView = [[MapOverlayView alloc] initWithOverlay:mapOverlay];
            UIImageView *imgV =[[UIImageView alloc] init];
            [imgV setContentMode:UIViewContentModeCenter];
            [imgV setFrame:CGRectMake(0, 0, self.mapOverlayView.frame.size.width, self.mapOverlayView.frame.size.height)];
            [imgV setCenter:self.mapOverlayView.center];
            [self.mapOverlayView addSubview:imgV];
        }
    }
    return self.mapOverlayView;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
}

@end
