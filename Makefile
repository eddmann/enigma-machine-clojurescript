RUN := docker run
VOLUME := -v $(PWD):/src -w /src
IMAGE := theasp/clojurescript-nodejs:shadow-cljs-alpine

dev:
	$(RUN) $(VOLUME) -v $(PWD)/.m2:/root/.m2:delegated -it -p 8020:8020 -p 9630:9630 $(IMAGE) bash -c "yarn && yarn dev"

release:
	$(RUN) $(VOLUME) -v $(PWD)/.m2:/root/.m2:delegated $(IMAGE) bash -c "yarn && yarn release"

test:
	$(RUN) $(VOLUME) -v $(PWD)/.m2:/root/.m2:delegated $(IMAGE) bash -c "yarn && yarn test"
