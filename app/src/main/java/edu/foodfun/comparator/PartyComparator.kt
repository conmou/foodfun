package edu.foodfun.comparator

import edu.foodfun.enums.FieldChangeType
import edu.foodfun.model.Party

class PartyComparator(private val partyChangeListner: PartyChangeListner? = null) : BaseComparator<Party>() {
    override fun compare(oldValue: Party?, newValue: Party) {
        checkTitleChanges(oldValue, newValue)
        checkMaxMemberChanges(oldValue, newValue)
        checkContentChanges(oldValue, newValue)
        checkStatusChanges(oldValue, newValue)
        checkUserChanges(oldValue, newValue)
        checkFeedbackListDoneList(oldValue, newValue)
//        onChatRecordChanged(oldValue, newValue)
    }

    private fun checkStatusChanges(oldValue: Party?, newValue: Party) {
        if (oldValue?.state == null && newValue.state != null)
            partyChangeListner?.onStatusChanged(FieldChangeType.ADDED, newValue.state!!)
        if (oldValue?.state != null && newValue.state != null && oldValue.state != newValue.state)
            partyChangeListner?.onStatusChanged(FieldChangeType.MODIFIED, newValue.state!!)
        if (oldValue?.state != null && newValue.state == null)
            partyChangeListner?.onStatusChanged(FieldChangeType.REMOVED, oldValue.state!!)
    }

    private fun checkContentChanges(oldValue: Party?, newValue: Party) {
        if (oldValue?.content == null && newValue.content != null)
            partyChangeListner?.onContentChanged(FieldChangeType.ADDED, newValue.content)
        if (oldValue?.content != null && newValue.content != null && oldValue.content != newValue.content)
            partyChangeListner?.onContentChanged(FieldChangeType.MODIFIED, newValue.content)
        if (oldValue?.content != null && newValue.content == null)
            partyChangeListner?.onContentChanged(FieldChangeType.REMOVED, oldValue.content)
    }

    private fun checkMaxMemberChanges(oldValue: Party?, newValue: Party) {
        if (oldValue?.maxMember == null && newValue.maxMember != null)
            partyChangeListner?.onMaxMemberChanged(FieldChangeType.ADDED, newValue.maxMember)
        if (oldValue?.maxMember != null && newValue.maxMember != null && oldValue.maxMember != newValue.maxMember)
            partyChangeListner?.onMaxMemberChanged(FieldChangeType.MODIFIED, newValue.maxMember)
        if (oldValue?.maxMember != null && newValue.maxMember == null)
            partyChangeListner?.onMaxMemberChanged(FieldChangeType.REMOVED, oldValue.maxMember)
    }

    private fun checkTitleChanges(oldValue: Party?, newValue: Party) {
        if (oldValue?.title == null && newValue.title != null)
            partyChangeListner?.onTitleChanged(FieldChangeType.ADDED, newValue.title)
        if (oldValue?.title != null && newValue.title != null && oldValue.title != newValue.title)
            partyChangeListner?.onTitleChanged(FieldChangeType.MODIFIED, newValue.title)
        if (oldValue?.title != null && newValue.title == null)
            partyChangeListner?.onTitleChanged(FieldChangeType.REMOVED, oldValue.title)
    }

    private fun checkUserChanges(oldValue: Party?, newValue: Party) {
        newValue.users.forEach { userId ->
            if (oldValue?.users == null)
                partyChangeListner?.onUserChanged(FieldChangeType.ADDED, userId)
            else if (oldValue.users.find { it == userId } == null)
                partyChangeListner?.onUserChanged(FieldChangeType.ADDED, userId)
        }
        oldValue?.users?.forEach { userId ->
            if (newValue.users.isEmpty())
                partyChangeListner?.onUserChanged(FieldChangeType.REMOVED, userId)
            else if (newValue.users.find { it == userId } == null)
                partyChangeListner?.onUserChanged(FieldChangeType.REMOVED, userId)
        }
    }

    private fun checkFeedbackListDoneList(oldValue: Party?, newValue: Party) {
        newValue.feedbacks.forEach { userId ->
            if (oldValue?.users == null)
                partyChangeListner?.onUserChanged(FieldChangeType.ADDED, userId)
            else if (oldValue.users.find { it == userId } == null)
                partyChangeListner?.onUserChanged(FieldChangeType.ADDED, userId)
        }
        oldValue?.feedbacks?.forEach { userId ->
            if (newValue.users.isEmpty())
                partyChangeListner?.onUserChanged(FieldChangeType.REMOVED, userId)
            else if (newValue.users.find { it == userId } == null)
                partyChangeListner?.onUserChanged(FieldChangeType.REMOVED, userId)
        }

    }
//    private fun onChatRecordChanged(oldValue: Party?, newValue: Party) {
//        newValue.chatRecord?.forEach { chatMap ->
//            if (oldValue?.chatRecord == null)
//                partyChangeListner?.onChatRecordChanged(FieldChangeType.ADDED, chatMap)
//            else if (oldValue.chatRecord.find { it == chatMap } == null)
//                partyChangeListner?.onChatRecordChanged(FieldChangeType.ADDED, chatMap)
//        }
//        oldValue?.chatRecord?.forEach { chatMap ->
//            if (newValue.chatRecord == null)
//                partyChangeListner?.onChatRecordChanged(FieldChangeType.REMOVED, chatMap)
//            else if (newValue.chatRecord.find { it == chatMap } == null)
//                partyChangeListner?.onChatRecordChanged(FieldChangeType.REMOVED, chatMap)
//        }
//    }


    open class PartyChangeListner {
        open fun onContentChanged(type: FieldChangeType, content: String){}
        open fun onMaxMemberChanged(type: FieldChangeType, maxMember: Int){}
        open fun onTitleChanged(type: FieldChangeType, title: String){}
        open fun onStatusChanged(type: FieldChangeType, status: String){}
        open fun onUserChanged(type: FieldChangeType, userId: String){}
        open fun onFeedbackListDoneListChanged(type: FieldChangeType, userId: String){}
    }
}