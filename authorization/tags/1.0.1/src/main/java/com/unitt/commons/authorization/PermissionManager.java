/*
 * Copyright 2009 UnitT Software Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unitt.commons.authorization;


import java.util.List;


/**
 * Manages the permissions in your permission repository
 * 
 * @author Josh Morris
 */
public interface PermissionManager
{
    /**
     * Fetches all permissions for the specified permissable that contain the
     * complete specified permission mask.
     * 
     * @param aMask
     *            permission mask to search for
     * @param aPermissable
     *            permissable to get permissions on
     * 
     * @return Empty list if none are found.
     */
    public List<AssignedPermission> getPermissions( long aMask, Permissable aPermissable );

    /**
     * Fetches all permissions for the specified permissable.
     * 
     * @param aPermissable
     *            permissable to get permissions on
     * 
     * @return Empty list if none are found.
     */
    public List<AssignedPermission> getPermissions( Permissable aPermissable );

    /**
     * Checks to see if a specified permission exists on a permissable for a
     * given list of assignables.
     * 
     * @param aPermission
     *            the permission mask to check for
     * @param aPermissable
     *            the permissable to check for permission on
     * @param aAssignables
     *            the list of assignables to check for a matching assigned
     *            permission
     * 
     * @return true if one of the assignables has an assigned permission in the
     *         repository that fully contains the specified permission
     */
    public boolean hasPermission( long aPermission, Permissable aPermissable, List<Assignable> aAssignables );

    /**
     * Add/subtracts the specified permission in the repository on the specified
     * permissable for each of the specified assignables. The resulting
     * permission change should preserve the existing permission and only modify
     * the bits in the permission mask. If the permission does not exist, it
     * will be created for addition operations, and ignored for all subtraction
     * operations. If the permission exists and subtracting the permission
     * results in a zero, it is acceptable to either remove the permission
     * entirely or store it as a zero.
     * 
     * @param aPermission
     *            permission mask to apply
     * @param aAdd
     *            true if an addition operation is desired, false if a
     *            subtraction operation is desired
     * @param aPermissable
     *            permissable to apply permission to
     * @param aAssignables
     *            assignables to apply permission to
     */
    public void applyPermission( long aPermission, boolean aAdd, Permissable aPermissable, List<Assignable> aAssignables );

    /**
     * Resets the specified permission in the repository on the specified
     * permissable for each of the specified assignables. The resulting
     * permission change should remove the existing permission and only set the
     * bits in the permission mask. If the permission does not exist, it will be
     * created.
     * 
     * @param aPermission
     *            permission mask to set
     * @param aPermissable
     *            permissable to set permission on
     * @param aAssignables
     *            assignables to set permission on
     */
    public void setPermission( long aPermission, Permissable aPermissable, List<Assignable> aAssignables );

    /**
     * Removes all permissions in the repository for the specified assignables.
     * It is acceptable to either remove the permission entirely or store it as
     * a zero.
     * 
     * @param aAssignables
     *            assignable to remove permissions for
     */
    public void removeAllPermissions( Assignable aAssignable );
}