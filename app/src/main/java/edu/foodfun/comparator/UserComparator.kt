package edu.foodfun.comparator

import com.google.firebase.firestore.GeoPoint
import edu.foodfun.enums.FieldChangeType
import edu.foodfun.model.User

class UserComparator(private val userChangeListener: UserChangeListener) : BaseComparator<User>() {
    override fun compare(oldValue: User?, newValue: User) {
        checkFriendChanges(oldValue, newValue)
        checkCurrentPartyChanges(oldValue, newValue)
        checkLocationChanges(oldValue, newValue)
        checkAvatarVersion(oldValue, newValue)
        checkGroupsChanges(oldValue, newValue)
        checkGroupContainChanges(oldValue, newValue)
        checkInviteChanges(oldValue, newValue)
    }

    private fun checkFriendChanges(oldValue: User?, newValue: User) {
        newValue.friends.forEach { userId ->
            if (oldValue?.friends == null)
                userChangeListener.onFriendChanged(FieldChangeType.ADDED, userId)
            else if (oldValue.friends.find { it == userId } == null)
                userChangeListener.onFriendChanged(FieldChangeType.ADDED, userId)
        }
        oldValue?.friends?.forEach { userId ->
            if (newValue.friends.isEmpty())
                userChangeListener.onFriendChanged(FieldChangeType.REMOVED, userId)
            else if (newValue.friends.find { it == userId } == null)
                userChangeListener.onFriendChanged(FieldChangeType.REMOVED, userId)
        }
    }

    private fun checkCurrentPartyChanges(oldValue: User?, newValue: User) {
        if (oldValue?.currentParty == null && newValue.currentParty != null)
            userChangeListener.onCurrentPartyChanged(FieldChangeType.ADDED, newValue.currentParty)
        else if (oldValue?.currentParty != null && newValue.currentParty != null && oldValue.currentParty != newValue.currentParty)
            userChangeListener.onCurrentPartyChanged(FieldChangeType.MODIFIED, newValue.currentParty)
        else if (oldValue?.currentParty != null && newValue.currentParty == null)
            userChangeListener.onCurrentPartyChanged(FieldChangeType.REMOVED, oldValue.currentParty)
    }

    private fun checkInviteChanges(oldValue: User?, newValue: User) {
        newValue.invites.forEach { userId ->
            if (oldValue?.invites == null)
                userChangeListener.onInviteChanged(FieldChangeType.ADDED, userId)
            else if (oldValue.invites.find { it == userId } == null)
                userChangeListener.onInviteChanged(FieldChangeType.ADDED, userId)
        }
        oldValue?.invites?.forEach { userId ->
            if (newValue.invites.isEmpty())
                userChangeListener.onInviteChanged(FieldChangeType.REMOVED, userId)
            else if (newValue.invites.find { it == userId } == null)
                userChangeListener.onInviteChanged(FieldChangeType.REMOVED, userId)
        }
    }

    private fun checkLocationChanges(oldValue: User?, newValue: User) {
        if (oldValue?.location == null && newValue.location != null)
            userChangeListener.onLocationChanged(FieldChangeType.ADDED, newValue.location)
        else if (oldValue?.location != newValue.location && newValue.location != null)
            userChangeListener.onLocationChanged(FieldChangeType.MODIFIED, newValue.location)
    }

    private fun checkAvatarVersion(oldValue: User?, newValue: User) {
        if (oldValue?.avatarVersion == null && newValue.avatarVersion != null)
            userChangeListener.onAvatarVersionChanged(FieldChangeType.ADDED, newValue.avatarVersion)
        else if (oldValue?.avatarVersion != null && newValue.avatarVersion != null && oldValue.avatarVersion ==  newValue.avatarVersion)
            userChangeListener.onAvatarVersionChanged(FieldChangeType.MODIFIED, newValue.avatarVersion)
        else if (oldValue?.avatarVersion != null && newValue.avatarVersion == null)
            userChangeListener.onAvatarVersionChanged(FieldChangeType.REMOVED, oldValue.avatarVersion)
    }

    private fun checkGroupsChanges(oldValue: User?, newValue: User) {
        newValue.groups.forEach { (groupName, _) ->
            if(oldValue?.groups == null)
                userChangeListener.onGroupChanged(FieldChangeType.ADDED, groupName)
            else if (!oldValue.groups.containsKey(groupName))
                userChangeListener.onGroupChanged(FieldChangeType.ADDED, groupName)
        }
        oldValue?.groups?.forEach { (groupName, _) ->
            if(newValue.groups.isEmpty())
                userChangeListener.onGroupChanged(FieldChangeType.REMOVED, groupName)
            else if(!newValue.groups.containsKey(groupName))
                userChangeListener.onGroupChanged(FieldChangeType.REMOVED, groupName)
        }
    }

    private fun checkGroupContainChanges(oldValue: User?, newValue: User) {
        val oldGroups = oldValue?.groups
        val newGroups = newValue.groups

        newGroups.forEach { (newGroupName, newList) ->
            newList.forEach { restaurantId ->
                if(oldGroups?.get(newGroupName)?.isEmpty() == true)
                    userChangeListener.onGroupRestaurantChanged(FieldChangeType.ADDED, newGroupName, restaurantId)
                else if(oldGroups?.get(newGroupName)?.find { it == restaurantId } == null)
                    userChangeListener.onGroupRestaurantChanged(FieldChangeType.ADDED, newGroupName, restaurantId)
            }
        }
        oldGroups?.forEach { (oldGroupName, oldList) ->
            oldList.forEach { restaurantId ->
                if(newGroups[oldGroupName]?.isEmpty() == true)
                    userChangeListener.onGroupRestaurantChanged(FieldChangeType.REMOVED, oldGroupName, restaurantId)
                else if(newGroups[oldGroupName]?.find { it == restaurantId } == null)
                    userChangeListener.onGroupRestaurantChanged(FieldChangeType.REMOVED, oldGroupName, restaurantId)
            }
        }
    }

    open class UserChangeListener {
        open fun onFriendChanged(type: FieldChangeType, userId: String){}
        open fun onCurrentPartyChanged(type: FieldChangeType, partyId: String){}
        open fun onAvatarVersionChanged(type: FieldChangeType, avatarVersion: Long){}
        open fun onLocationChanged(type: FieldChangeType, location: GeoPoint){}
        open fun onGroupChanged(type: FieldChangeType, groupName: String){}
        open fun onGroupRestaurantChanged(type: FieldChangeType, groupName: String, restaurantId: String){}
        open fun onInviteChanged(type: FieldChangeType, userId: String){}
    }
}