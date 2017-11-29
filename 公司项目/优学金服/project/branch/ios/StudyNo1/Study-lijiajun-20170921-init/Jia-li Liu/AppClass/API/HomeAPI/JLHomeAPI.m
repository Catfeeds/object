//
//  JLHomeAPI.m
//  Jia-li Liu
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import "JLHomeAPI.h"

@implementation JLHomeAPI

+(instancetype)getHomeList{
    
    JLHomeAPI *api = [self new];
    
    api.subUrl = @"http://116.211.167.106/api/live/aggregation?uid=133825214&interest=1";
    api.parameters = @{};
    return api;
}


+ (NSDictionary *)objectClassInArray{
    return @{@"lives" : [Lives class]};
}
@end
@implementation Lives

@end


@implementation Extra

+ (NSDictionary *)objectClassInArray{
    return @{@"label" : [Label class]};
}

@end


@implementation Label

@end


@implementation Creator

@end


