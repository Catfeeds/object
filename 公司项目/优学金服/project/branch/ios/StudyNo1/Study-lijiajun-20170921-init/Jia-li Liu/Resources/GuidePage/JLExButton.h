//
//  JLExButton.h
//  NewFeatureDemo
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//
#import <UIKit/UIKit.h>
@class JLExButton;
typedef void(^tapAction)(JLExButton *senderButton);

@interface JLExButton : UIButton
/**按钮文字*/
@property (nonatomic, strong) UILabel *XLJLable;
/**按钮图片*/
@property (nonatomic, strong) UIImageView *XLJImageView;

- (void)setTItleOriginX:(CGFloat)x sizeWidth:(CGFloat)width;
- (void)setImageOriginX:(CGFloat)x sizeWidth:(CGFloat)width;

- (void)setTitleCenterWithWidth:(CGFloat)width;
- (void)setImageCenterWithWidth:(CGFloat)width;

- (void)setTitleFrame:(CGRect)frame;
- (void)setImageFrame:(CGRect)frame;

/**点击事件*/
- (void)tapAction:(tapAction)action;


@end
