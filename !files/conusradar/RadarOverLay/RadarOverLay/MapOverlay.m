//
//  MapOverlay.m
//  RadarImageExample
//
//  Created by Neon Spark on 3/8/12.
//  Copyright (c) 2012 http://sugartin.info. All rights reserved.
//

#import "MapOverlay.h"

@implementation MapOverlay
@synthesize coordinate,radarData;

- (id) initWithLowerLeftCoordinate:(CLLocationCoordinate2D)lowerLeftCoordinate withUpperRightCoordinate:(CLLocationCoordinate2D)upperRightCoordinate {
    
    MKMapPoint lowerLeft = MKMapPointForCoordinate(lowerLeftCoordinate);
    MKMapPoint upperRight = MKMapPointForCoordinate(upperRightCoordinate);
    
    mapRect = MKMapRectMake(lowerLeft.x, upperRight.y, upperRight.x - lowerLeft.x, lowerLeft.y - upperRight.y);
    
    return self;
    
}

- (CLLocationCoordinate2D)coordinate
{
    return MKCoordinateForMapPoint(MKMapPointMake(MKMapRectGetMidX(mapRect), MKMapRectGetMidY(mapRect)));
}

- (MKMapRect)boundingMapRect
{
    return mapRect;
}

/*
- (id)initWithCoordinate:(CLLocationCoordinate2D)coordinate {
    self = [super init];
    if (self != nil) {
        
        
    }
    return self;
}

- (CLLocationCoordinate2D)coordinate
{
    CLLocationCoordinate2D coord1 = {
        37.434999,-122.16121
    };
    
    return coord1;
}

- (MKMapRect)boundingMapRect
{
    
    MKMapPoint upperLeft = MKMapPointForCoordinate(self.coordinate);
    
    MKMapRect bounds = MKMapRectMake(upperLeft.x, upperLeft.y, 2000, 2000);
    return bounds;
}

*/
@end
