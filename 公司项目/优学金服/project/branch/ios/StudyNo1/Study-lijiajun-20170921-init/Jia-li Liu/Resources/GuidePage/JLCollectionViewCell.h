//
//  JLCollectionViewCell.h
//  NewFeatureDemo
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//
#import <UIKit/UIKit.h>
@class JLExButton;
@interface JLCollectionViewCell : UICollectionViewCell
/**滚动图片*/
@property (nonatomic, strong) UIImage *image;
@property (nonatomic, assign) CGFloat startHeight;
@property (nonatomic, strong) UIViewController *toController;
@property (nonatomic, strong) JLExButton *startButton;
@property (nonatomic, strong) NSString *imageNameStr;

/**
 *  判断当前页码和最后一个页码
 *
 *  @param currentPage 当前页码
 *  @param lastIndex   最后一个页码
 */
- (void)setCurrentPageIndex:(NSInteger)currentPage lastPageIndex:(NSInteger)lastIndex;



@end
