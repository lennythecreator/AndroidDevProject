package com.example.gympal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gympal.adapters.ChatAdapter
import com.example.gympal.adapters.ChatMessage
import com.example.gympal.network.ApiClient
import com.example.gympal.network.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ChatBottomSheet : BottomSheetDialogFragment() {

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var progressChatLoading: ProgressBar
    private lateinit var chatAdapter: ChatAdapter
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = SessionManager.userId(requireContext())
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        rvChatMessages = view.findViewById(R.id.rvChatMessages)
        etChatInput = view.findViewById(R.id.etChatInput)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        progressChatLoading = view.findViewById(R.id.progressChatLoading)
        view.findViewById<ImageButton>(R.id.btnCloseChat).setOnClickListener {
            dismiss()
        }

        chatAdapter = ChatAdapter(mutableListOf())
        rvChatMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvChatMessages.adapter = chatAdapter

        btnSendMessage.setOnClickListener {
            sendMessage()
        }

        etChatInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                progressChatLoading.visibility = View.VISIBLE
                val response = ApiClient.get("/users/$userId/insights/chat")
                val messagesArray = response.optJSONArray("messages")
                val messages = mutableListOf<ChatMessage>()
                
                if (messagesArray != null) {
                    for (i in 0 until messagesArray.length()) {
                        val msgObj = messagesArray.getJSONObject(i)
                        messages.add(
                            ChatMessage(
                                id = msgObj.optInt("id", 0),
                                role = msgObj.optString("role", ""),
                                content = msgObj.optString("content", ""),
                                createdAt = msgObj.optString("created_at", "")
                            )
                        )
                    }
                }
                
                chatAdapter.setMessages(messages)
                scrollToBottom()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load chat history", Toast.LENGTH_SHORT).show()
            } finally {
                progressChatLoading.visibility = View.GONE
            }
        }
    }

    private fun sendMessage() {
        val messageText = etChatInput.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        etChatInput.setText("")
        btnSendMessage.isEnabled = false
        progressChatLoading.visibility = View.VISIBLE

        // Add user message to UI immediately
        val userMessage = ChatMessage(
            id = 0,
            role = "user",
            content = messageText,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.addMessage(userMessage)
        scrollToBottom()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val payload = JSONObject().put("message", messageText)
                val response = ApiClient.post("/users/$userId/insights/chat", payload)
                
                // Update with full response from server
                val messagesArray = response.optJSONArray("messages")
                val messages = mutableListOf<ChatMessage>()
                
                if (messagesArray != null) {
                    for (i in 0 until messagesArray.length()) {
                        val msgObj = messagesArray.getJSONObject(i)
                        messages.add(
                            ChatMessage(
                                id = msgObj.optInt("id", 0),
                                role = msgObj.optString("role", ""),
                                content = msgObj.optString("content", ""),
                                createdAt = msgObj.optString("created_at", "")
                            )
                        )
                    }
                }
                
                chatAdapter.setMessages(messages)
                scrollToBottom()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                // Reload chat history to remove the failed user message
                loadChatHistory()
            } finally {
                btnSendMessage.isEnabled = true
                progressChatLoading.visibility = View.GONE
            }
        }
    }

    private fun scrollToBottom() {
        rvChatMessages.post {
            if (chatAdapter.itemCount > 0) {
                rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            ChatBottomSheet().show(fragmentManager, "ChatBottomSheet")
        }
    }
}

