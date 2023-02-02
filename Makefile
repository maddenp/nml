GN=$(GRAALVM)/native-image
JARDIR=target/default+uberjar
JARFILE=$(JARDIR)/$(NAME).jar
NAME=nml
NATIVE=target/nml

.PHONY: clean native test

$(JARFILE): src/$(NAME)/core.clj
	lein uberjar

native: $(NATIVE)

$(NATIVE): $(JARFILE)
	@if [ -z "$(GRAALVM)" ]; then echo "Please set GRAALVM to point to your GraalVM installation"; false; fi
	@if ! which $(GN) >/dev/null; then echo "Could not find $(GN). Run 'gu install native-image' perhaps?'"; false; fi
	$(GN) -jar $< --no-fallback -o $(JARDIR)/$(NAME)

clean:
	$(RM) -frv target

test:
	lein test
