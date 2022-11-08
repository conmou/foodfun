package edu.foodfun.comparator

import edu.foodfun.enums.FieldChangeType
import edu.foodfun.model.Message

class MessageComparator(private val messageChangeListener: MessageChangeListner): BaseComparator<Message>() {
    override fun compare(oldValue: Message?, newValue: Message) {
        TODO("Not yet implemented")
    }

    private fun checkMessageChanges() {

    }

    open class MessageChangeListner {
        open fun onMessageChanged(type: FieldChangeType, messageId: String){}
    }
}