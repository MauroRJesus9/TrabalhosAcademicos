using HabitAqui.Models;
using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels
{
    public class PesquisaEmpresaViewModel
    {

        public List<Empresa>? ListaDeEmpresas { get; set; }
        public int NumResultados { get; set; }
        [Display(Name = "PESQUISA DE EMPRESAS ...", Prompt = "Introduza o texto a pesquisar")]
        public string? TextoAPesquisar { get; set; }

    }
}
