package org.cscie88c.kafka.model

import org.apache.kafka.streams.kstream.ValueJoiner

case class TransactionWithAccount(txn: Transaction, acct: String)

/**
 * This allows us to do joins between streams and tables
 * It takes 3 types:
 *   Left side of join
 *   Right side of join
 *   The output object
 */
class AccountJoiner extends ValueJoiner[Transaction,String,TransactionWithAccount]{

  override def apply(txn: Transaction, acct: String): TransactionWithAccount = {
    TransactionWithAccount(txn, acct) 
  }
}
