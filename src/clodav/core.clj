(ns clodav.core
  (:use [ring.adapter.jetty])
  (:import [org.eclipse.jetty.server Server]
           [org.eclipse.jetty.server.handler AbstractHandler]
           [io.milton.http ResourceFactory HttpManager AuthenticationService]
           [io.milton.config HttpManagerBuilder]
           [io.milton.servlet ServletRequest ServletResponse]
           [io.milton.resource
            Resource
            PropFindableResource
            GetableResource
            PutableResource
            CollectionResource]))




(do

  (def files
    {"File 1.txt" "Here is the contents of file 1."
     "File 2.txt" "Here is the contents of file 2."})

  (def childResource
    (reify
      PropFindableResource
      (getCreateDate [this] (java.util.Date.))

      Resource
      (authenticate [this user pass] :authenticated)
      (authorise [this request method auth] true)
      (getRealm [this] "test@somewhere.com")
      (getUniqueId [this] "uniqueID")
      (getName [this] "CHILD")
      (getModifiedDate [this] (java.util.Date.))
      (checkRedirect [this request] nil)))

  (defn to-resource [name content]
    (reify
      PropFindableResource
      (getCreateDate [this] (java.util.Date.))

      Resource
      (authenticate [this user pass] :authenticated)
      (authorise [this request method auth] true)
      (getRealm [this] "test@somewhere.com")
      (getUniqueId [this] (str "file" name))
      (getName [this] name)
      (getModifiedDate [this] (java.util.Date.))
      (checkRedirect [this request] nil)

      GetableResource
      (getContentLength [this] (Long/valueOf (count content)))
      (getContentType [this accepts] (println "GetContentType" name) "text/plain")
      (getMaxAgeSeconds [this auth] (println "GetMaxAge" name) nil)
      (sendContent [this out range params content-type]
        (println "Send Content" out)
        (.write out (.getBytes content))
        (.close out))

      ))

  (def rootResource
    (reify
      PropFindableResource
      (getCreateDate [this] (java.util.Date.))

      CollectionResource
      (getChildren [this] (println "Get children")
        (let [file-resources (map #(to-resource % (get files %)) (keys files))]
          (java.util.ArrayList. file-resources)))
      (child [this name] (println "Get Child ******* " name) (get files name))

      Resource
      (authenticate [this user pass] :authenticated)
      (authorise [this request method auth] true)
      (getRealm [this] "test@somewhere.com")
      (getUniqueId [this] "ROOT")
      (getName [this] "")
      (getModifiedDate [this] (java.util.Date.))
      (checkRedirect [this request] nil)

      PutableResource
      (createNew [this new-name in length content-type]
        (println "Create new:" new-name " Length:" length " Content type:" content-type))))

  (def resource-factory (reify ResourceFactory
                          (getResource [this host path]
                            (println "REQ RESOURCE:" host path)
                            (cond (= path "/") rootResource
                                  :else (let [name    (.substring path 1)
                                              content (get files name)]
                                          (to-resource name content))))))

  (def http-manager-builder (HttpManagerBuilder.))

  (.setResourceFactory http-manager-builder resource-factory)

  (.setEnableOptionsAuth http-manager-builder false)
  (.setEnableBasicAuth http-manager-builder false)
  (.setEnableDigestAuth http-manager-builder false)


  (.init http-manager-builder)
  (def http-manager (.buildHttpManager http-manager-builder))


  (def handler
    (bound-fn [target base-req req resp]
      (println req)
      (.process http-manager (ServletRequest. req nil) (ServletResponse. resp)))))

(def s (Server. 8080))

(.setHandler s (proxy [AbstractHandler] []
                 (handle [target base-req req resp]
                   (#'handler target base-req req resp))))
(.start s)

(.stop s)
