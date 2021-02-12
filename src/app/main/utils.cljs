(ns app.main.utils)

(defn send-ipc [window event arg]
  (let [web-content (.-webContents window)]
    (.send web-content event arg)))
