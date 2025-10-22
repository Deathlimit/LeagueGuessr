package com.example.leagueguessr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Fragment_ChampionList : Fragment() {

    private lateinit var adapter: Adapter_Champions
    private var role: String = ""
    private var onChampionSelected: ((Data_champion) -> Unit)? = null

    companion object {
        private const val ARG_ROLE = "role"

        fun newInstance(role: String, onChampionSelected: (Data_champion) -> Unit): Fragment_ChampionList {
            val fragment = Fragment_ChampionList()
            val args = Bundle()
            args.putString(ARG_ROLE, role)
            fragment.arguments = args
            fragment.onChampionSelected = onChampionSelected
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_ROLE)?.let { role = it }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_champion_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val champions = getChampionsForRole(role)

        adapter = Adapter_Champions(champions) { champion ->
            onChampionSelected?.invoke(champion)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4, GridLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }

    private fun getChampionsForRole(role: String): List<Data_champion> {
        val championsByRole = mutableListOf<Data_champion>()
        val drawableResources = getDrawableChampions()

        drawableResources.forEach { (resourceName, resourceId) ->
            val roles = extractRolesFromFileName(resourceName)
            val name = extractChampionName(resourceName)

            if (roles.contains(role)) {
                val champion = Data_champion(
                    id = resourceId,
                    name = name,
                    imageResId = resourceId,
                    roles = roles
                )
                championsByRole.add(champion)
            }
        }

        return championsByRole
    }

    private fun extractChampionName(fileName: String): String {
        return fileName.removePrefix("_champion_")
            .substringAfter("_")
            .replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun getDrawableChampions(): Map<String, Int> {
        val resources = mutableMapOf<String, Int>()
        val fields = R.drawable::class.java.fields
        for (field in fields) {
            val resourceName = field.name
            if (resourceName.startsWith("_champion_")) {
                val resourceId = field.getInt(null)
                resources[resourceName] = resourceId
            }
        }
        return resources
    }

    private fun extractRolesFromFileName(fileName: String): List<String> {
        val pattern = "_champion_(\\d+)_.+".toRegex()
        val result = pattern.find(fileName)

        val roles = mutableListOf<String>()
        result?.groupValues?.get(1)?.let { roleDigits ->
            roleDigits.forEach { digit ->
                roles.add(digit.toString())
            }
        }
        return roles
    }
}