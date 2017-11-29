//
//  UIViewController+JTNavigationExtension.h
//  JTNavigationController
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import <UIKit/UIKit.h>
#import "JTNavigationController.h"

@interface UIViewController (JTNavigationExtension)

@property (nonatomic, assign) BOOL jt_fullScreenPopGestureEnabled;

@property (nonatomic, weak) JTNavigationController *jt_navigationController;

@end
