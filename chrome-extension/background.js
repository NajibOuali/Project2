// Background Service Worker for Manifest V3 Chrome Extension

chrome.runtime.onInstalled.addListener(() => {
  // Create context menu item on right-click text selection
  chrome.contextMenus.create({
    id: "translate-to-darija",
    title: "Translate to Darija",
    contexts: ["selection"]
  });
});

// Handle Context Menu clicks
chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (info.menuItemId === "translate-to-darija" && info.selectionText) {
    const selectedText = info.selectionText;

    // Open side panel in the current window
    if (tab && tab.windowId) {
      await chrome.sidePanel.open({ windowId: tab.windowId });
    }

    // Save the selected text to storage and broadcast to side panel
    await chrome.storage.local.set({ pendingTranslation: selectedText });

    // Send a message to active sidepanel if already listening
    chrome.runtime.sendMessage({
      action: "AUTO_TRANSLATE",
      text: selectedText
    }).catch(() => {
      // Side panel may still be opening; it will read from chrome.storage on load
    });
  }
});

// Enable side panel when action icon is clicked
chrome.sidePanel
  .setPanelBehavior({ openPanelOnActionClick: true })
  .catch((error) => console.error(error));
