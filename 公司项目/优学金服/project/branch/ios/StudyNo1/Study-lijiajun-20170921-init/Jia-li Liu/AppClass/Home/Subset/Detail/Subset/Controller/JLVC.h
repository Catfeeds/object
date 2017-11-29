//
//  HJHealthRecordsVC.h
//  MilkPo_User
//
//  Created by  on 2017/8/19.
//  Copyright © 2017年 . All rights reserved.
//
#import "JLBaseViewController.h"
#import "iCarousel.h"

@interface JLVC : JLBaseViewController<iCarouselDataSource,iCarouselDelegate>

@property (nonatomic, strong) iCarousel *carousel;

@property (nonatomic,assign) BOOL wrap;

@end
