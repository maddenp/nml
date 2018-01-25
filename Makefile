X=nml

.PHONY: clean test

$(X).tgz: $(X) $(X).jar
	tar cvzf $@ $^

$(X).jar: target/uberjar/$(X).jar
	cp -vr $< $@

target/uberjar/$(X).jar: src/$(X)/core.clj
	lein uberjar

test:
	lein test

clean:
	$(RM) -fr $(X).jar $(X).tgz target
