(ns novo-challenge.subs
  (:require
   [re-frame.core :as rf]
   [novo-challenge.utils :as utils]))

(rf/reg-sub
 ::loading-characters
 (fn [db]
   (:loading-characters db)))

(rf/reg-sub
 ::view-id
 (fn [db]
   (:view-id db)))

(rf/reg-sub
 ::loading-films
 (fn [db]
   (:loading-films db)))

(rf/reg-sub
 ::film-data
 (fn [db]
   (:film-data db)))

(rf/reg-sub
 ::character-data
 (fn [db]
   (:character-data db)))

(rf/reg-sub
 ::character-map
 (fn []
   (rf/subscribe [::character-data]))
 (fn [character-data]
   (reduce #(assoc %1
                   (keyword (utils/get-id-from-url (:url %2)))
                   (select-keys %2 [:name :gender :skin_color :hair_color :birth_year]))
           {} character-data)))

(rf/reg-sub
 ::film-data
 (fn [db]
   (:film-data db)))

;A map with keywords based on film-id, values are maps of film data.
(rf/reg-sub
 ::film-map-with-endpoints
 (fn []
   (rf/subscribe [::film-data]))
 (fn [film-data]
   (reduce #(assoc %1
                   (keyword (utils/get-id-from-url (:url %2)))
                   (select-keys %2 [:title :release_date :characters]))
           {} film-data)))

;Composition of functions. Separated for readability.
(def extract-id->keyword
  (comp keyword str utils/get-id-from-url))

;Replaces vectors of API endpoints with character IDs in film-map.
(rf/reg-sub
 ::film-map
 (fn []
   (rf/subscribe [::film-map-with-endpoints]))
 (fn [film-map-with-endpoints]
   (reduce-kv (fn [new-map k v]
                (assoc new-map k
                       (update-in v [:characters] #(map extract-id->keyword %))))
              {}
              film-map-with-endpoints)))
