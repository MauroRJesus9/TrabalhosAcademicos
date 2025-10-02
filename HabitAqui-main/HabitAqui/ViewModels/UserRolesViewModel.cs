using HabitAqui.Models;

namespace HabitAqui.ViewModels
{
    public class UserRolesViewModel{

        public string UserId { get; set; }
        /*public string PrimeiroNome { get; set; }
        public string UltimoNome { get; set; }*/
        public string NomeCompleto { get; set; }
        public int NIF { get; set; }
        public int EmpresaId { get; set; }
        public Empresa? Empresa { get; set; }
        public IEnumerable<string> Roles { get; set; }
    }

}
