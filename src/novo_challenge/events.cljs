(ns novo-challenge.events
  (:require
   [re-frame.core :as rf]
   [novo-challenge.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))


(rf/reg-event-db
 ::select-character-view-id
 (fn [db [_ val]]
   (assoc db :view-id val)))

(rf/reg-event-db
 ::return-to-home-screen
 (fn [db [_ val]]
   (assoc db :view-id 0)))

;This is called recursively. Conjoins data map with new character data.
(rf/reg-event-db
 ::add-to-character-list
 (fn [db [_ val]]
   (update db :character-data into val)))

;Similar event to above, but with side effect setting loading-characters false.
(rf/reg-event-db
 ::add-to-character-list-final
 (fn [db [_ val]]
   (-> db
       (update :character-data into val)
       (assoc :loading-characters false))))

;Fetch data from SWAPI for all Star Wars films.
(rf/reg-event-fx
  ::fetch-movies
  (fn [{:keys [db]} _]
    {:db   (assoc db :loading-films true)
     :http-xhrio {:method          :get
                  :uri             "https://swapi.dev/api/films"
                  :timeout         80000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::fetch-movies-success]
                  :on-failure      [::fetch-movies-failure]}}))

(rf/reg-event-db
 ::fetch-movies-success
 (fn [db [_ {:keys [results]}]]
   (-> db
       (assoc :loading-films false)
       (assoc :film-data results))))

(rf/reg-event-db
 ::fetch-movies-failure
 (fn [db [_ result]]
   (println result)
   (assoc db :last-error result)))

;Fetch data from SWAPI recursively for all Star Wars characters.
(rf/reg-event-fx
  ::fetch-characters
  (fn [{:keys [db]} _]
    {:db   (assoc db :loading-characters true)
     :http-xhrio {:method           :get
                  :uri              (:uri db)
                  :timeout          80000
                  :response-format  (ajax/json-response-format {:keywords? true})
                  :on-success       [::fetch-characters-success]
                  :on-failure       [::fetch-characters-failure]}}))

(rf/reg-event-fx
 ::fetch-characters-success
 (fn [cotx [_ {:keys [results next]}]]
   (if (not= next nil)
     {:dispatch      [::add-to-character-list results]
      :http-xhrio    {:method            :get
                      :uri               next
                      :timeout           80000
                      :response-format   (ajax/json-response-format {:keywords? true})
                      :on-success        [::fetch-characters-success]
                      :on-failure        [::fetch-characters-failure]}}
     {:dispatch      [::add-to-character-list-final results]})))

(rf/reg-event-db
 ::fetch-characters-failure
 (fn [db [_ result]]
   (assoc db :last-error result)))
