//
//  JSONSerializer.m
//  UnittJSONSerializer
//
//  Created by Josh Morris on 5/27/11.
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

#import "JSONSerializer.h"


#pragma mark -


@implementation JSONSerializer

@synthesize objectHandler;


#pragma mark Deserialize
- (void) fillObjectFromData:(NSData*) aData object:(id) aObject
{
    //create dictionary from data
    NSDictionary* data = [aData objectFromJSONDataWithParseOptions:parseOptions];
    
    //fill object using dictionary
    [self.objectHandler fillObjectFromDictionary:data object:aObject];
}

- (void) fillObjectFromString:(NSString*) aData object:(id) aObject
{
    //create dictionary from string
    NSDictionary* data = [aData objectFromJSONStringWithParseOptions:parseOptions];
    
    //fill object using dictionary
    [self.objectHandler fillObjectFromDictionary:data object:aObject];
}

- (id) deserializeObjectFromData:(NSData*) aData type:(Class) aClass
{
    id result = nil;
    //if not type is specified - let JSON figure it out
    if (!aClass)
    {
        result = [aData objectFromJSONDataWithParseOptions:parseOptions];
        
        if (!result)
        {
            return [[[NSString alloc] initWithData:aData encoding:NSUTF8StringEncoding] autorelease];
        }
    }
    
    //create object of the specified type
    result = [[aClass alloc] init];
    
    //fill object using deserialized JSON
    [self fillObjectFromData:aData object:result];
    
    return result;
}

- (id) deserializeObjectFromString:(NSString*) aData type:(Class) aClass
{
    //create object of the specified type
    id result = [[aClass alloc] init];
    
    //fill object using deserialized JSON
    [self fillObjectFromString:aData object:result];
    
    return result;
}

- (NSArray*) deserializeArrayFromType:(NSArray*) aClasses dataArray:(NSArray*) aData
{
    //init
    NSMutableArray* results = [NSMutableArray array];
    
    //loop through array
    int length = aClasses.count;
    if (aData && aData.count < length)
    {
        length = aData.count;
    }
    for (int i = 0; i < length; i++) 
    {
        id result = [aData objectAtIndex:i];
        Class type = [aClasses objectAtIndex:i];
        [results addObject:[self deserializeObjectFromData:result type:type]];
    }
    
    return results;
}

- (NSArray*) deserializeArrayFromType:(NSArray*) aClasses data:(NSData*) aData
{
    //create array from data
    return [self deserializeArrayFromType:aClasses dataArray:[aData objectFromJSONDataWithParseOptions:parseOptions]];
}

- (NSArray*) deserializeArrayFromType:(NSArray*) aClasses string:(NSString*) aData
{
    //create array from string
    return [self deserializeArrayFromType:aClasses dataArray:[aData objectFromJSONStringWithParseOptions:parseOptions]];
}


#pragma mark Serialize
- (NSData*) serializeToDataFromObject:(id) aObject
{
    //convert the object to JSON data
    return [[self.objectHandler objectToDictionary:aObject] JSONDataWithOptions:serializeOptions error:nil];
}

- (NSString*) serializeToStringFromObject:(id) aObject
{
    //convert the object to a JSON string
    NSError* error;
    NSString* value = [[self.objectHandler objectToDictionary:aObject] JSONStringWithOptions:serializeOptions error:&error];
    NSLog(@"Error serializing: %@", error.localizedDescription);
    return value;
}

- (NSData*) serializeToDataFromArray:(NSArray*) aObjects
{
    //init
    NSMutableArray* arrayOfObjectDictionaries = [NSMutableArray array];
    
    //convert each item to dictionary
    for (id item in aObjects) 
    {
        if ([item isKindOfClass:[NSString class]])
        {
            [arrayOfObjectDictionaries addObject:item];
        }
        else
        {
            [arrayOfObjectDictionaries addObject:[self.objectHandler objectToDictionary:item]];
        }
    }
    
    //convert array of dictionaries to JSON data
    return [arrayOfObjectDictionaries JSONDataWithOptions:serializeOptions error:nil];
}

- (NSString*) serializeToStringFromArray:(NSArray*) aObjects
{
    //init
    NSMutableArray* arrayOfObjectDictionaries = [NSMutableArray array];
    
    //convert each item to dictionary
    for (id item in aObjects) 
    {
        if ([item isKindOfClass:[NSString class]])
        {
            [arrayOfObjectDictionaries addObject:item];
        }
        else
        {
            [arrayOfObjectDictionaries addObject:[self.objectHandler objectToDictionary:item]];
        }
    }
    
    //convert array of dictionaries to JSON data
    return [arrayOfObjectDictionaries JSONStringWithOptions:serializeOptions error:nil];
}


#pragma mark Lifecycle
+ (id) serializerWithParseOptions: (JSParseOptionFlags) aParseOptions serializeOptions: (JSSerializeOptionFlags) aSerializeOptions
{
    return [[[JSONSerializer alloc] initWithParseOptions:aParseOptions serializeOptions:aSerializeOptions] autorelease];
}

+ (id) serializerWithParseOptions: (JSParseOptionFlags) aParseOptions serializeOptions: (JSSerializeOptionFlags) aSerializeOptions objectHandler:(ObjectHandler*) aObjectHandler
{
    return [[[JSONSerializer alloc] initWithParseOptions:aParseOptions serializeOptions:aSerializeOptions objectHandler:aObjectHandler] autorelease];
}

- (id) initWithParseOptions:(JSParseOptionFlags) aParseOptions serializeOptions:(JSSerializeOptionFlags) aSerializeOptions objectHandler:(ObjectHandler*) aObjectHandler
{
    self = [super init];
    if (self) 
    {
        parseOptions = aParseOptions;
        serializeOptions = aSerializeOptions;
        objectHandler = [aObjectHandler retain];
    }
    return self;
}

- (id) initWithParseOptions: (JSParseOptionFlags) aParseOptions serializeOptions: (JSSerializeOptionFlags) aSerializeOptions
{
    self = [super init];
    if (self) 
    {
        parseOptions = aParseOptions;
        serializeOptions = aSerializeOptions;
        objectHandler = [[ObjectHandler alloc] init];
    }
    return self;
}

- (id) init
{
    self = [super init];
    if (self) 
    {
        parseOptions = JSParseOptionsStrict;
        serializeOptions = JSSerializeOptionNone;
        objectHandler = [[ObjectHandler alloc] init];
    }
    return self;
}

- (void) dealloc 
{
    [objectHandler release];
    
    [super dealloc];
}


@end