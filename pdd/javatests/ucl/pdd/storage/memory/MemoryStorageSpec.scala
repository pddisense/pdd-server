package ucl.pdd.storage.memory

import ucl.pdd.storage.{Storage, StorageSpec}

/**
 * Unit tests for [[MemoryStorage]].
 */
class MemoryStorageSpec extends StorageSpec {
  behavior of "MemoryStorage"

  override protected def createStorage: Storage = new MemoryStorage
}
