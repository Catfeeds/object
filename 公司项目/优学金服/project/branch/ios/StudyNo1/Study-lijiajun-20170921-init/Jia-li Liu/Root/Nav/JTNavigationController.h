//
//  JTNavigationController.h
//  JTNavigationController
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import <UIKit/UIKit.h>

@interface JTWrapViewController : UIViewController

@property (nonatomic, strong, readonly) UIViewController *rootViewController;

+ (JTWrapViewController *)wrapViewControllerWithViewController:(UIViewController *)viewController;

@end

@interface JTNavigationController : UINavigationController

@property (nonatomic, strong) UIImage *backButtonImage;

@property (nonatomic, assign) BOOL fullScreenPopGestureEnabled;

@property (nonatomic, copy, readonly) NSArray *jt_viewControllers;

@end
