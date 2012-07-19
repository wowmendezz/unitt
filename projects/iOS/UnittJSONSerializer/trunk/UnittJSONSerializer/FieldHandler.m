//
//  DefaultFieldHandler.m
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

#import "FieldHandler.h"

@implementation FieldHandler


@synthesize dateMultiplier;
@synthesize dateFormatter;
@synthesize dateStoredAs;


#pragma mark - Conversion
- (NSNumber*) fromDateToNumber:(NSDate*) aDate {
    long long value = self.dateMultiplier * [aDate timeIntervalSince1970];
    return [NSNumber numberWithLongLong:value];
}

- (NSDate*) toDateFromNumber:(NSNumber*) aValue {
    double value = [aValue doubleValue];
    if (self.dateMultiplier != 0) {
        value = value / self.dateMultiplier;
    }
    return [NSDate dateWithTimeIntervalSince1970:value];
}

- (NSString*) fromDateToString:(NSDate*) aDate {
    return [self.dateFormatter stringFromDate:aDate];
}

- (NSDate*) toDateFromString:(NSString*) aValue {
    return [self.dateFormatter dateFromString:aValue];
}


#pragma mark - Field Handling
- (id) getFieldValueForInvocation:(NSInvocation*) aInvocation datatype:(JSDataType) aDataType {
    switch (aDataType) {
        case JSDataTypeInt:
        {
            int value;
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return [NSNumber numberWithInt:value];
        }
        case JSDataTypeLong:
        {
            long long value = 0;
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return [NSNumber numberWithLongLong:value];
        }
        case JSDataTypeDouble:
        {
            double value = 0;
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return [NSNumber numberWithDouble:value];
        }
        case JSDataTypeBoolean:
        {
            BOOL value = NO;
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return value ? @"true" : @"false";
        }
        case JSDataTypeNSNumber:
        {
            NSNumber* value = [[[NSNumber alloc] init] autorelease];
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return value;
        }
        case JSDataTypeNSDate:
        {
            NSDate* value = [[[NSDate alloc] init] autorelease];
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            if (value) {
                switch (self.dateStoredAs) {
                    case JSDateStoredAsNumber:
                        return [self fromDateToNumber: value];
                    case JSDateStoredAsString:
                        return [self fromDateToString:value];
                }
            }
            return nil;
        }
        default:
        {
            id value;
            [aInvocation invoke];
            [aInvocation getReturnValue:&value];
            return value;
        }
    }
}

- (void) setFieldValueForInvocation:(NSInvocation*) aInvocation datatype:(JSDataType) aDataType value:(id) aValue {
    switch (aDataType) {
        case JSDataTypeInt:
            if ([aValue isKindOfClass:[NSString class]]) {
                int value = [((NSString*) aValue) intValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if ([aValue isKindOfClass:[NSNumber class]]) {
                int value = [((NSNumber*) aValue) intValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                int value = 0;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to int", NSStringFromClass([aValue class]));
            }
            break;
        case JSDataTypeLong:
            if ([aValue isKindOfClass:[NSString class]]) {
                long long value = [((NSString*) aValue) longLongValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if ([aValue isKindOfClass:[NSNumber class]]) {
                long long value = [((NSNumber*) aValue) longLongValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                long long value = 0;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to long", NSStringFromClass([aValue class]));
            }
            break;
        case JSDataTypeDouble:
            if ([aValue isKindOfClass:[NSString class]]) {
                double value = [((NSString*) aValue) doubleValue];
//                NSLog(@"parsed double value as: %f", value);
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if ([aValue isKindOfClass:[NSNumber class]]) {
                double value = [((NSNumber*) aValue) doubleValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                double value = 0;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to double", NSStringFromClass([aValue class]));
            }
            break;
        case JSDataTypeBoolean:
            if ([aValue isKindOfClass:[NSString class]]) {
                BOOL value = [((NSString*) aValue) boolValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                BOOL value = NO;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to boolean", NSStringFromClass([aValue class]));
            }
            break;
        case JSDataTypeNSNumber:
            if ([aValue isKindOfClass:[NSString class]]) {
                long long almostValue = [((NSString*) aValue) longLongValue];
                NSNumber* value = [NSNumber numberWithLongLong:almostValue];
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if ([aValue isKindOfClass:[NSNumber class]]) {
                [aInvocation setArgument:&aValue atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                NSDate* value = nil;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to NSNumber", NSStringFromClass([aValue class]));
            }
            break;
        case JSDataTypeNSDate:
            if ([aValue isKindOfClass:[NSString class]]) {
                NSDate* value = nil;
                double almostValue = 0;
                switch (self.dateStoredAs) {
                    case JSDateStoredAsNumber:
                        almostValue = [((NSString*) aValue) doubleValue];
                        value = [self toDateFromNumber:[NSNumber numberWithDouble:almostValue]];
                        break;
                    case JSDateStoredAsString:
                        value = [self toDateFromString:(NSString*) aValue];
                        break;
                }
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if ([aValue isKindOfClass:[NSNumber class]]) {
                NSDate* value = nil;
                switch (self.dateStoredAs) {
                    case JSDateStoredAsNumber:
                        value = [self toDateFromNumber:(NSNumber*) aValue];
                        break;
                }
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else if (aValue == nil || [aValue isKindOfClass:[NSNull class]]) {
                NSDate* value = nil;
                [aInvocation setArgument:&value atIndex:2];
                [aInvocation invoke];
            }
            else {
                NSLog(@"No custom handling logic to convert from %@ to NSDate", NSStringFromClass([aValue class]));
            }
            break;
        default:
            [aInvocation setArgument:&aValue atIndex:2];
            [aInvocation invoke];
            break;
    }
}


#pragma mark - Lifecycle
- (id)init {
    self = [super init];
    if (self) {
        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss Z"];
        dateFormatter.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"UTC"];
        dateFormatter.locale = [[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"] autorelease];
        dateMultiplier = 1000;
    }

    return self;
}

- (id)initWithDateMultiplier:(int)aDateMultiplier {
    self = [self init];
    if (self) {
        dateMultiplier = aDateMultiplier;
    }

    return self;
}

- (id)initWithDateFormatter:(NSDateFormatter*) aDateFormatter {
    self = [self init];
    if (self) {
        self.dateFormatter = aDateFormatter;
    }

    return self;
}

+ (id)fieldHandlerWithDateMultiplier:(int)aDateMultiplier {
    return [[[FieldHandler alloc] initWithDateMultiplier:aDateMultiplier] autorelease];
}

+ (id)fieldHandlerWithDateFormatter:(NSDateFormatter*) aDateFormatter {
    return [[[FieldHandler alloc] initWithDateFormatter:aDateFormatter] autorelease];
}

- (void)dealloc {
    self.dateFormatter = nil;
    [super dealloc];
}


@end
