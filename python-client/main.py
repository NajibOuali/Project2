"""
GUI Desktop Application using Tkinter for LLM Darija Translator.
"""
import tkinter as tk
from tkinter import ttk, messagebox
import threading
from config import DEFAULT_API_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD
from api_client import TranslationApiClient

class DarijaTranslatorApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("🇲🇦 LLM Darija Translator")
        self.geometry("700x650")
        self.minsize(550, 500)

        self.style = ttk.Style(self)
        self.style.theme_use("clam")

        self.setup_ui()

    def setup_ui(self):
        # Header
        header_frame = tk.Frame(self, bg="#c1272d", pady=12)
        header_frame.pack(fill="x")
        title_label = tk.Label(header_frame, text="🇲🇦 LLM Darija Translator", font=("Segoe UI", 16, "bold"), fg="#ffffff", bg="#c1272d")
        title_label.pack()

        # Main Container
        main_frame = tk.Frame(self, padx=16, pady=12)
        main_frame.pack(fill="both", expand=True)

        # Settings / Configuration (Collapsible LabelFrame)
        settings_frame = ttk.LabelFrame(main_frame, text=" API & Authentication Settings ", padding=10)
        settings_frame.pack(fill="x", pady=(0, 10))

        # API URL
        ttk.Label(settings_frame, text="API URL:").grid(row=0, column=0, sticky="w", padx=4, pady=2)
        self.url_var = tk.StringVar(value=DEFAULT_API_URL)
        url_entry = ttk.Entry(settings_frame, textvariable=self.url_var, width=45)
        url_entry.grid(row=0, column=1, columnspan=3, sticky="we", padx=4, pady=2)

        # Username
        ttk.Label(settings_frame, text="Username:").grid(row=1, column=0, sticky="w", padx=4, pady=2)
        self.user_var = tk.StringVar(value=DEFAULT_USERNAME)
        user_entry = ttk.Entry(settings_frame, textvariable=self.user_var, width=18)
        user_entry.grid(row=1, column=1, sticky="w", padx=4, pady=2)

        # Password
        ttk.Label(settings_frame, text="Password:").grid(row=1, column=2, sticky="w", padx=4, pady=2)
        self.pass_var = tk.StringVar(value=DEFAULT_PASSWORD)
        pass_entry = ttk.Entry(settings_frame, textvariable=self.pass_var, show="•", width=18)
        pass_entry.grid(row=1, column=3, sticky="w", padx=4, pady=2)

        settings_frame.columnconfigure(1, weight=1)

        # Translation Area (Source & Target)
        trans_frame = tk.Frame(main_frame)
        trans_frame.pack(fill="both", expand=True)

        # Source Text
        src_label_frame = tk.Frame(trans_frame)
        src_label_frame.pack(fill="x", pady=(4, 2))
        ttk.Label(src_label_frame, text="Source Text (English / Any):", font=("Segoe UI", 10, "bold")).pack(side="left")
        
        self.clear_btn = ttk.Button(src_label_frame, text="🗑️ Clear", command=self.clear_text)
        self.clear_btn.pack(side="right")

        self.src_text = tk.Text(trans_frame, height=5, font=("Segoe UI", 11), wrap="word")
        self.src_text.pack(fill="both", expand=True, pady=(0, 10))
        self.src_text.insert("1.0", "Hello, how are you?")

        # Action Buttons & Status
        btn_frame = tk.Frame(main_frame)
        btn_frame.pack(fill="x", pady=(0, 10))

        self.translate_btn = tk.Button(
            btn_frame,
            text="Translate to Moroccan Darija ➔",
            font=("Segoe UI", 11, "bold"),
            bg="#c1272d",
            fg="#ffffff",
            activebackground="#9e1f24",
            activeforeground="#ffffff",
            cursor="hand2",
            command=self.start_translation
        )
        self.translate_btn.pack(fill="x", ipady=6)

        # Loading / Progress Bar
        self.progress = ttk.Progressbar(main_frame, mode="indeterminate")

        # Target Translation Result
        res_label_frame = tk.Frame(main_frame)
        res_label_frame.pack(fill="x", pady=(4, 2))
        ttk.Label(res_label_frame, text="Moroccan Darija (الدارجة المغربية):", font=("Segoe UI", 10, "bold")).pack(side="left")

        self.copy_btn = ttk.Button(res_label_frame, text="📋 Copy", command=self.copy_translation)
        self.copy_btn.pack(side="right")

        self.res_text = tk.Text(main_frame, height=5, font=("Segoe UI", 14), wrap="word", bg="#f8fafc")
        self.res_text.pack(fill="both", expand=True, pady=(0, 10))

        # Status Bar
        self.status_var = tk.StringVar(value="Ready.")
        status_bar = tk.Label(self, textvariable=self.status_var, bd=1, relief="sunken", anchor="w", font=("Segoe UI", 9), fg="#64748b", bg="#f1f5f9", padx=8, pady=4)
        status_bar.pack(side="bottom", fill="x")

    def clear_text(self):
        self.src_text.delete("1.0", "end")
        self.res_text.delete("1.0", "end")
        self.status_var.set("Cleared.")

    def copy_translation(self):
        content = self.res_text.get("1.0", "end").strip()
        if content:
            self.clipboard_clear()
            self.clipboard_append(content)
            self.status_var.set("✅ Copied to clipboard!")
        else:
            self.status_var.set("No text to copy.")

    def start_translation(self):
        text = self.src_text.get("1.0", "end").strip()
        if not text:
            messagebox.showwarning("Empty Text", "Please enter some text to translate.")
            return

        # UI State update for loading
        self.translate_btn.config(state="disabled")
        self.progress.pack(fill="x", pady=(0, 8), before=self.res_text)
        self.progress.start(10)
        self.status_var.set("Translating via LLM backend...")

        # Run in separate thread to keep UI responsive
        threading.Thread(target=self._run_translation_worker, args=(text,), daemon=True).start()

    def _run_translation_worker(self, text):
        api_url = self.url_var.get().strip()
        username = self.user_var.get().strip()
        password = self.pass_var.get().strip()

        client = TranslationApiClient(api_url=api_url, username=username, password=password)

        try:
            result = client.translate(text)
            translated_text = result.get("translatedText", "")
            self.after(0, self._handle_success, translated_text)
        except Exception as e:
            self.after(0, self._handle_error, str(e))

    def _handle_success(self, translated_text):
        self.progress.stop()
        self.progress.pack_forget()
        self.translate_btn.config(state="normal")
        
        self.res_text.delete("1.0", "end")
        self.res_text.insert("1.0", translated_text)
        self.status_var.set("Translation complete.")

    def _handle_error(self, err_message):
        self.progress.stop()
        self.progress.pack_forget()
        self.translate_btn.config(state="normal")
        
        self.status_var.set(f"Error: {err_message}")
        messagebox.showerror("Translation Error", err_message)

def main():
    app = DarijaTranslatorApp()
    app.mainloop()

if __name__ == "__main__":
    main()
