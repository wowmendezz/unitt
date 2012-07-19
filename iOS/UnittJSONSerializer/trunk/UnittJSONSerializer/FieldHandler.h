//
//  DefaultFieldHandler.h
//  UnittJSONSerializer
//
//  Created by Josh Morris on 9/9/11.
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

#import <Foundation/Foundation.h>
#import <Foundation/NSObjCRuntime.h>
#import <objc/runtime.h>
#import "JSONPropertyInfo.h"


enum {
    JSDateStoredAsNumber = 0, //default
    JSDateStoredAsString = 1
};
typedef NSUInteger JSDateStoredAsFlags;


@interface FieldHandler : NSObject {
    int dateMultiplier;
    NSDateFormatter* dateFormatter;
    JSDateStoredAsFlags dateStoredAs;
}

@property(nonatomic, assign) int dateMultiplier;
@property(nonatomic, assign) JSDateStoredAsFlags dateStoredAs;
@property(retain) NSDateFormatter* dateFormatter;


- (NSNumber *)fromDateToNumber:(NSDate *)aDate;
- (NSDate *)toDateFromNumber:(NSNumber *)aValue;
- (NSString *)fromDateToString:(NSDate *)aDate;
- (NSDate *)toDateFromString:(NSString *)aValue;

- (id) getFieldValueForInvocation:(NSInvocation*) aInvocation datatype:(JSDataType) aDataType;
- (void) setFieldValueForInvocation:(NSInvocation*) aInvocation datatype:(JSDataType) aDataType value:(id) aValue;


- (id)initWithDateMultiplier:(int)aDateMultiplier;
+ (id)fieldHandlerWithDateMultiplier:(int)aDateMultiplier;

- (id)initWithDateFormatter:(NSDateFormatter*) aDateFormatter;
+ (id)fieldHandlerWithDateFormatter:(NSDateFormatter*) aDateFormatter;


@end
