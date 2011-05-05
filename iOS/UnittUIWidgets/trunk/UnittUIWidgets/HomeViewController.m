//
//  HomeView.m
//  UnittUIWidgets
//
//  Created by Josh Morris on 4/22/11.
//  Copyright 2011 UnitT Software. All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not
//  use this file except in compliance with the License. You may obtain a copy of
//  the License at
// 
//  http://www.apache.org/licenses/LICENSE-2.0
// 
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
//  License for the specific language governing permissions and limitations under
//  the License.
//

#import "HomeViewController.h"
#import "IconModel.h"


@interface HomeViewController()
@property (readonly) HomeModel* model;
@property (readonly) HomeView* homeView;
@property (assign) BOOL isDirty;
@end

@implementation HomeViewController

@synthesize startColor, endColor;
@synthesize itemSize, margin, toolbarHeight;
@synthesize useToolbar;
@synthesize isDirty;
@synthesize toolbarItems;


//properties logic
//--------------------------------------------------------------------------------
- (HomeModel*) model
{
    if (!model) 
    {
        model= [[HomeModel alloc] init];
    }
    
    return model;
}

- (HomeView*) homeView
{
    return (HomeView*) self.view;
}


//model logic
//--------------------------------------------------------------------------------
- (void) handleModelChange
{
    //tell view to reload if needed, else mark dirty to draw later
    if (self.view && self.view.window)
    {
        [self.homeView setNeedsLayout];
        self.isDirty = false;
    }
    else
    {
        self.isDirty = true;
    }
}

- (void) addItem: (NSString*) aKey controller: (UIViewController*) aController icon: (UIImage*) aIcon label: (NSString*) aLabelText
{        
    //add to model
    [self.model addItem:aKey controller:aController icon:aIcon label:aLabelText];
    [self handleModelChange];
}

- (void) removeItem: (NSString*) aKey
{
    //add to model
    [self.model removeItem:aKey];
    [self handleModelChange];
}


//controller logic
//--------------------------------------------------------------------------------
- (void) didSelectItem: (UIButton*) aSender
{
    if (self.navigationController)
    {
        NSArray* items = self.model.iconModels;
        if (items.count > aSender.tag)
        {
            IconModel* item = [items objectAtIndex:aSender.tag];
            if (item)
            {
                @try
                {
                    [self.navigationController pushViewController:item.viewController animated:YES];
                }
                @catch (NSException* e)
                {
                    NSLog(@"Missing view controller");
                    UIAlertView* view = [[[UIAlertView alloc] initWithTitle:@"Missing Item" message:@"Could not find item to show" delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
                    [view show];
                }
            }
        }
    }
}

- (NSArray*) getHomeItems
{
    return self.model.iconModels;
}


//lifecycle logic
//--------------------------------------------------------------------------------
- (void) loadView
{
    HomeView* myView = [[HomeView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame] delegate:self datasource:self];
    self.view = myView;
    [myView release];
}

- (void) viewWillAppear: (BOOL) aAnimated
{
    if (self.isDirty)
    {
        self.isDirty = false;
    }
    [super viewWillAppear:aAnimated];
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) aFromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:aFromInterfaceOrientation];
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)aToInterfaceOrientation
{
    return YES;
}

- (void) setup
{
    self.startColor = [UIColor whiteColor];
    self.endColor = [UIColor blackColor];
    self.itemSize = CGSizeMake(92, 120);
    self.margin = CGSizeMake(10, 10);
    self.useToolbar = false;
    self.toolbarHeight = 32;
}

- (id)init 
{
    self = [super init];
    if (self) 
    {
        [self setup];
    }
    return self;
}

- (id) initWithToolbarItems:(NSArray *)aItems
{
    self = [super init];
    if (self) 
    {
        [self setup];
        self.useToolbar = true;
        toolbarItems = aItems;
    }
    return self;
}

- (void)dealloc 
{
    [toolbarItems release];
    [startColor release];
    [endColor release];
    [model release];
    [super dealloc];
}

@end
