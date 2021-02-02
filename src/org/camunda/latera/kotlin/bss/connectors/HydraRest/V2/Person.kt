package org.camunda.latera.bss.connectors.HydraRest.V2

import org.camunda.latera.bss.HttpClient.HydraRestClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.LinkedHashMap


class Person(
  val url: String,
  val port: Int,
  val token: String,
  val useSSL: Boolean = true
) {
  private val client = HydraRestClient.getClient(this.url, this.port, this.token, this.useSSL)

  enum class SubjState(val state: Int) {
    Off(1011),
    On(2011),
    Locked(3011),
    Disabled(4011),
    ManuallySuspended(5011),
    PaymentsLocked(6011),
    CLIssuingSuspended(7011),
  }

  enum class Gender(val state: Int) {
    Male(1138),
    Female(2138)
  }

  enum class SubjType(val state: Int) {
    Person(18001)
  }

//  Дописать все параметры
  data class CreateParams(
    val vc_first_name: String,
    val vc_surname: String,
    val vc_second_name: String,
    val n_subj_state_id: SubjState = SubjState.On)

  data class CreateResponse(
    val person: Person
  )

  data class Person(
    val n_person_id: Long,
    val n_opf_id: Long?,
    val vc_opf_code: String?,
    val vc_opf_name: String?,
    val vc_first_name: String,
    val vc_surname: String,
    val vc_second_name: String?,
    val n_sex_id: Gender,
    val vc_sex: String,
    val vc_inn: String?,
    val n_doc_auth_type_id: Long?,
    val vc_doc_auth_type_code: String?,
    val vc_doc_auth_type_name: String?,
    val vc_doc_serial: String?,
    val vc_doc_no: String?,
    val d_doc: Date?,
    val vc_document: String?,
    val vc_doc_department: String?,
    val d_birth: Date?,
    val vc_birth_place: String,
    val vc_pens_insurance: String?,
    val vc_med_insurance: String?,
    val n_subject_id: Long,
    val n_subj_type_id: SubjType,
    val n_parent_subj_id: Long,
    val n_subj_state_id: SubjState,
    val vc_subj_state_name: String,
    val n_base_subject_id: Long,
    val n_firm_id: Long,
    val vc_firm: String,
    val n_region_id: Long?,
    val vc_region: String?,
    val n_owner_id: Long,
    val vc_subj_name: String,
    val vc_subj_code: String,
    val vc_name: String,
    val vc_code: String,
    val d_created: Date,
    val n_creator_id: Long,
    val t_tags: Array<String>,
    val vc_tags: String?,
    val vc_rem: String?,
    val vc_code_upper: String,
    val vc_name_upper: String,
    val n_subj_group_id: Long,
    val vc_subj_group_name: String?,
    val n_citizenship_id: Long,
    val vc_citizenship: String?,
    val vc_kpp: String?,
    val additional_values: Array<LinkedHashMap<String, Any>>)

  fun create(params: CreateParams): Person {
    val client = this.client

    val response = runBlocking {
      client.post<CreateResponse> {
        url {
          encodedPath = "/rest/v2/subjects/persons/"
        }

        body = mapOf("person" to params)
      }
    }

    return response.person
  }
}
