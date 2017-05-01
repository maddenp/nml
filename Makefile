X=nml

.PHONY: clean

$(X).tgz: $(X) $(X).jar
	tar cvzf $@ $^

$(X).jar: target/uberjar/$(X).jar
	cp -vr $< $@

target/uberjar/$(X).jar: src/$(X)/core.clj
	lein uberjar

clean:
	$(RM) -fr $(X).jar $(X).tgz target
