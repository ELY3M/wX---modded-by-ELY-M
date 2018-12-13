//
//  MapOverlay.h
//  RadarImageExample
//
//  Created by Neon Spark on 3/8/12.
//  Copyright (c) 2012 http://sugartin.info. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface MapOverlay : NSObject<MKOverlay>{
    MKMapRect mapRect;
}


@property (nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property (nonatomic, retain) NSData *radarData;

- (MKMapRect)boundingMapRect;
- (id) initWithLowerLeftCoordinate:(CLLocationCoordinate2D)lowerLeftCoordinate withUpperRightCoordinate:(CLLocationCoordinate2D)upperRightCoordinate ;
@end
