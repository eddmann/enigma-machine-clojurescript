RUN := docker run
VOLUMES := -v $(PWD):/src -w /src -v $(PWD)/.m2:/root/.m2:delegated
IMAGE := theasp/clojurescript-nodejs:shadow-cljs-alpine

dev:
	$(RUN) $(VOLUMES) -it -p 8020:8020 -p 9630:9630 $(IMAGE) bash -c "yarn && yarn dev"

release:
	$(RUN) $(VOLUMES) $(IMAGE) bash -c "yarn && yarn release"

test:
	$(RUN) $(VOLUMES) $(IMAGE) bash -c "yarn && yarn test"

shell:
	$(RUN) $(VOLUMES) -it $(IMAGE) bash
