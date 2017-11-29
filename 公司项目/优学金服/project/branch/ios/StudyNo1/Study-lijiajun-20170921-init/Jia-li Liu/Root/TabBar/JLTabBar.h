//
//  JLTabBar.h
//  KLIANS
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//

#import <UIKit/UIKit.h>

@class JLTabBar;

@protocol JLTabBarDelegate <NSObject>
@optional
- (void)tabBarPlusBtnClick:(JLTabBar *)tabBar;
@end


@interface JLTabBar : UITabBar

/** tabbar的代理 */
@property (nonatomic, weak) id<JLTabBarDelegate> myDelegate ;

@end
