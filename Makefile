GN=$(GRAALVM)/native-image
JARDIR=target/default+uberjar
NAME=nml
NATIVE=target/nml
TARGETS=clean native test uberjar
UBERJAR=$(JARDIR)/$(NAME).jar

.PHONY: $(TARGETS)

all:
	$(error Valid targets are: $(TARGETS))

clean:
	$(RM) -frv target

native: $(NATIVE)

test:
	lein test

uberjar: $(UBERJAR)

$(NATIVE): $(UBERJAR)
	@if [ -z "$(GRAALVM)" ]; then echo "Please set GRAALVM to point to your GraalVM root"; false; fi
	@if ! which $(GN) >/dev/null; then echo "Could not find $(GN). Run 'gu install native-image' perhaps?'"; false; fi
	$(GN) -jar $< --no-fallback -o $(JARDIR)/$(NAME)

$(UBERJAR): src/$(NAME)/core.clj
	lein uberjar
