(ns novo-challenge.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [novo-challenge.events :as events]
   [novo-challenge.views :as views]
   [novo-challenge.config :as config]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch [::events/fetch-movies])
  (rf/dispatch [::events/fetch-characters])
  (dev-setup)
  (mount-root))
