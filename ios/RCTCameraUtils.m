//
//  RCTCameraUtils.m
//  Exponent
//
//  Created by Stanisław Chmiela on 23.10.2017.
//  Copyright © 2017 650 Industries. All rights reserved.
//

#import "RCTCameraUtils.h"

@implementation RCTCameraUtils

# pragma mark - Camera utilities

+ (AVCaptureDevice *)deviceWithMediaType:(AVMediaType)mediaType preferringPosition:(AVCaptureDevicePosition)position
{
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:mediaType];
    AVCaptureDevice *captureDevice = [devices firstObject];
    
    for (AVCaptureDevice *device in devices) {
        if ([device position] == position) {
            captureDevice = device;
            break;
        }
    }
    
    return captureDevice;
}

# pragma mark - Enum conversion

+ (AVCaptureVideoOrientation)videoOrientationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    switch (orientation) {
        case UIInterfaceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIInterfaceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIInterfaceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIInterfaceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return 0;
    }
}

+ (AVCaptureVideoOrientation)videoOrientationForDeviceOrientation:(UIDeviceOrientation)orientation
{
    switch (orientation) {
        case UIDeviceOrientationPortrait:
            return AVCaptureVideoOrientationPortrait;
        case UIDeviceOrientationPortraitUpsideDown:
            return AVCaptureVideoOrientationPortraitUpsideDown;
        case UIDeviceOrientationLandscapeLeft:
            return AVCaptureVideoOrientationLandscapeRight;
        case UIDeviceOrientationLandscapeRight:
            return AVCaptureVideoOrientationLandscapeLeft;
        default:
            return AVCaptureVideoOrientationPortrait;
    }
}

+ (float)temperatureForWhiteBalance:(RCTCameraWhiteBalance)whiteBalance
{
    switch (whiteBalance) {
        case RCTCameraWhiteBalanceSunny: default:
            return 5200;
        case RCTCameraWhiteBalanceCloudy:
            return 6000;
        case RCTCameraWhiteBalanceShadow:
            return 7000;
        case RCTCameraWhiteBalanceIncandescent:
            return 3000;
        case RCTCameraWhiteBalanceFluorescent:
            return 4200;
    }
}

+ (NSString *)captureSessionPresetForVideoResolution:(RCTCameraVideoResolution)resolution
{
    switch (resolution) {
        case RCTCameraVideo2160p:
            return AVCaptureSessionPreset3840x2160;
        case RCTCameraVideo1080p:
            return AVCaptureSessionPreset1920x1080;
        case RCTCameraVideo720p:
            return AVCaptureSessionPreset1280x720;
        case RCTCameraVideo4x3:
            return AVCaptureSessionPreset640x480;
        default:
            return AVCaptureSessionPresetHigh;
    }
}

@end

