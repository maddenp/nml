GN=$(GRAALVM)/bin/native-image
NAME=nml
TARGETS=clean native test uberjar
UBERJAR=target/$(NAME).jar

.PHONY: $(TARGETS)

all:
	$(error Valid targets are: $(TARGETS))

clean:
	clojure -T:build $@

native: $(NAME)

test:
	clojure -X:test

uberjar: $(UBERJAR)

$(NAME): $(UBERJAR)
	@if [ -z "$(GRAALVM)" ]; then echo "Please set GRAALVM to point to your GraalVM root"; false; fi
	@if ! which $(GN) >/dev/null; then echo "Could not find $(GN). Run 'gu install native-image' perhaps?'"; false; fi
	$(GN) -jar $< --no-fallback -o $@

$(UBERJAR): src/$(NAME)/core.clj
	clojure -T:build uberjar
