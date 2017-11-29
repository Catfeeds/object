//
//  UIViewController+JTNavigationExtension.m
//  JTNavigationController
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import "UIViewController+JTNavigationExtension.h"
#import <objc/runtime.h>

@implementation UIViewController (JTNavigationExtension)

- (BOOL)jt_fullScreenPopGestureEnabled {
    return [objc_getAssociatedObject(self, _cmd) boolValue];
}

- (void)setJt_fullScreenPopGestureEnabled:(BOOL)fullScreenPopGestureEnabled {
    objc_setAssociatedObject(self, @selector(jt_fullScreenPopGestureEnabled), @(fullScreenPopGestureEnabled), OBJC_ASSOCIATION_RETAIN);
}

- (JTNavigationController *)jt_navigationController {
    return objc_getAssociatedObject(self, _cmd);
}

- (void)setJt_navigationController:(JTNavigationController *)navigationController {
    objc_setAssociatedObject(self, @selector(jt_navigationController), navigationController, OBJC_ASSOCIATION_ASSIGN);
}

@end
