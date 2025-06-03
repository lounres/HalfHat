package dev.lounres.kone.maybe


public inline fun <Element> Maybe<Element>.useIfSome(block: (Element) -> Unit) {
    when (this) {
        None -> {}
        is Some<Element> -> block(this.value)
    }
}