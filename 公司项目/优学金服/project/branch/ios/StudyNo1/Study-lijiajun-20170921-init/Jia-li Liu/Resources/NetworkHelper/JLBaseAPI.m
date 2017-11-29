//
//  JLBaseAPI.m
//  Jia-li Liu
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//
#import "JLBaseAPI.h"

@interface JLBaseAPI ()


@end

@implementation JLBaseAPI


- (JLNetworkClient *)netWorkClient {
    return [JLNetworkClient networkClientWithSubUrl:self.subUrl parameters:self.parameters files:self.uploadFile JLBaseAPI:self];
}

- (NSMutableDictionary *)parameters {
    
    if (!_parameters) {
        
        _parameters = [NSMutableDictionary dictionary];
    }
    
    return _parameters;
}

@end
