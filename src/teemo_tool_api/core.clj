(ns teemo-tool-api.core
  (:gen-class))

(require '[clojure.java.jdbc :as sql]
         '[clojure.core.async :as async]
         '[ring.adapter.jetty :as jetty]
         '[ring.util.response :refer [response]]
         '[ring.middleware.json :refer [wrap-json-response wrap-json-body]]
         '[ring.middleware.cors :refer [wrap-cors]]
         '[compojure.core :refer :all]
         '[compojure.route :as route]
         )

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "/home/delta/db/database.db"})

; table for the test data
(defn create-db-test-table
  [db]
  (sql/db-do-commands db
                      (sql/create-table-ddl :test_bets
                                            [[:bettor :text]
                                             [:amount :integer]
                                             [:team :text]
                                             [:timestamp :datetime :default :current_timestamp]]
                                            {:conditional? true})))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn generate-random-bet
  []
  (let [bettor-name (rand-str 20)
        bet-size (+ (rand-int 15000) 3)
        bet-team (rand-nth ["blue" "red"])]
    {:bettor bettor-name
     :amount bet-size
     :team bet-team}))

(defn insert-test-bet
  [db bet-map]
  (sql/insert! db :test_bets bet-map))


(defn bets-for-team
  [db team]
  (let [team (clojure.string/lower-case team)
        results (sql/query db ["SELECT amount FROM bets WHERE team = ?
AND
timestamp > datetime((SELECT MAX(timestamp) FROM bets), '-5 minutes')
" team])]
    (map (fn [row] (get row :amount)) results)
    ))

(defn test-bets-for-team
  [db team]
  (let [team (clojure.string/lower-case team)
        results (sql/query db ["SELECT amount FROM test_bets WHERE team = ?
AND
timestamp > datetime((SELECT MAX(timestamp) FROM bets), '-5 minutes')
" team])]
    (map (fn [row] (get row :amount)) results)
    ))

;(bets-for-team db "blue")

(defn handle-team-request
  [team]
  (response {:bets (bets-for-team db team)}))

(defn handle-test-team-request
  [team]
  (response {:bets (test-bets-for-team db team)}))

(defn test-generate
  [db bet-count]
  (let [bets (repeatedly bet-count #(generate-random-bet))]
    (prn bets)
    (doseq [bet bets]
      (do
      (insert-test-bet db bet)
      (Thread/sleep 2000)))
    (prn "finished inserting test data")))

; The test generation endpoint/handler is intended for frontend
; developers who want to see how their changes affect the site
; without having to wait for a new game on stream. Slowly inserts
; ~10-50 bets into the test table which is accessed via the
; standard endpoints prepended with /test
(defn handle-test-generate-request
  [db]
  (let [bet-count (+ 10 (rand-int 40))]
    (sql/execute! db ["DROP TABLE test_bets"])
    (create-db-test-table db)
    (async/thread (test-generate db bet-count))
    (response {:started-data-gen "true"})))

; Easter egg. Patrick Rothfuss is, surprisingly, a regular bettor
; on the Salty Teemo stream. Queries his bets specifically.
(defn handle-rothfuss-latest
  [db]
  (let [query-result (sql/query db ["SELECT team, amount, timestamp FROM bets WHERE bettor = ? AND timestamp = (SELECT MAX(timestamp) FROM bets WHERE bettor = ?)" "PatrickRothfuss" "PatrickRothfuss"])
        bet-row (first query-result)
        ]
    (response bet-row)))

(defn handle-rothfuss-all
  [db]
  (let [query-result (sql/query db ["SELECT team, amount, timestamp FROM bets WHERE bettor = ?" "PatrickRothfuss"])]
    (response query-result)))

(def app-routes
  (routes
   (GET "/bets/blue" request (handle-team-request "blue"))
   (GET "/bets/red" request (handle-team-request "red"))
   (GET "/test/bets/generate" request (handle-test-generate-request db))
   (GET "/test/bets/blue" request (handle-test-team-request "blue"))
   (GET "/test/bets/red" request (handle-test-team-request "red"))
   (GET "/bets/rothfuss/latest" request (handle-rothfuss-latest db))
   (GET "/bets/rothfuss/all" request (handle-rothfuss-all db))
   (route/not-found {:error "not found"})))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response
      (wrap-cors :access-control-allow-methods [:get]
                 :access-control-allow-headers ["Content-Type" "application/json"
                                                ]
                 :access-control-allow-origin [#".*"])
      ))


;(def server (jetty/run-jetty app {:port 3008
;                      :join? false}))

;(.stop server)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (create-db-test-table db)

  (jetty/run-jetty app {:port 3000}))
