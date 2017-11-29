//
//  JLBaseAPI.h
//  Jia-li Liu
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import <Foundation/Foundation.h>
#import "JLNetworkClient.h"

@interface JLBaseAPI : NSObject

@property (nonatomic, assign) NetworkCodeType code;

@property (nonatomic, strong) NSString *msg;

@property (nonatomic, strong) NSMutableDictionary *parameters;

@property (nonatomic, strong) NSArray *uploadFile;

@property (nonatomic, copy) NSString *subUrl;

@property (nonatomic, assign) id<JLDataHandlerProtocol>dataHandler;


/**
 *  API类网络请求客户端
 *
 *  @return API类网络请求客户端
 */
- (JLNetworkClient *)netWorkClient;


@end

